 /*******************************************************************************
  * Copyright (c) 2013 Xilinx, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Xilinx - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IMemory;
 import org.eclipse.tcf.services.IRegisters;
 import org.eclipse.tcf.services.IRegisters.RegistersContext;
 import org.eclipse.tcf.services.IRunControl;
 
 public class ElfLoader implements Runnable {
 
     private final IChannel channel;
 
     /* Loader parameters */
     private Map<String,Object> args;
     private boolean download;
     private boolean set_pc;
     private Runnable done;
 
     /* Services */
     private final IRunControl service_rc;
     private final IMemory service_mem;
     private final IRegisters service_regs;
 
     /* Pending commands */
     private final Set<IToken> cmds = new HashSet<IToken>();
 
     /* Debug contexts */
     private final Map<String,IRunControl.RunControlContext> contexts =
             new HashMap<String,IRunControl.RunControlContext>();
 
     /* Running debug contexts */
     private final Set<String> running = new HashSet<String>();
 
     private List<Throwable> errors = new ArrayList<Throwable>();
 
     private final IRunControl.RunControlListener rc_listener = new IRunControl.RunControlListener() {
         @Override
         public void contextSuspended(String id, String pc, String reason, Map<String, Object> params) {
             running.remove(id);
             run();
         }
         @Override
         public void contextResumed(String id) {
             running.add(id);
             run();
         }
         @Override
         public void contextRemoved(String[] context_ids) {
             for (String id : context_ids) {
                 contexts.remove(id);
                 running.remove(id);
             }
             run();
         }
         @Override
         public void contextException(String context, String msg) {
         }
         @Override
         public void contextChanged(IRunControl.RunControlContext[] contexts) {
         }
         @Override
         public void contextAdded(IRunControl.RunControlContext[] arr) {
             for (IRunControl.RunControlContext ctx : arr) {
                 String id = ctx.getID();
                 contexts.put(id, ctx);
                 if (ctx.hasState()) running.add(id);
             }
             run();
         }
         @Override
         public void containerSuspended(String context, String pc, String reason,
                 Map<String, Object> params, String[] suspended_ids) {
             for (String id : suspended_ids) running.remove(id);
             run();
         }
         @Override
         public void containerResumed(String[] context_ids) {
             for (String id : context_ids) running.add(id);
             run();
         }
     };
 
     private final IRunControl.DoneGetContext done_ctx_get_context = new IRunControl.DoneGetContext() {
         @Override
         public void doneGetContext(IToken token, Exception error, final IRunControl.RunControlContext context) {
             cmds.remove(token);
             if (error != null) {
                 errors.add(error);
             }
             else {
                 final String id = context.getID();
                 contexts.put(id, context);
                 if (context.hasState()) {
                     cmds.add(context.getState(new IRunControl.DoneGetState() {
                         @Override
                         public void doneGetState(IToken token, Exception error,
                                 boolean suspended, String pc, String reason, Map<String, Object> params) {
                             cmds.remove(token);
                             if (error != null) {
                                 errors.add(error);
                             }
                             else if (!suspended) {
                                 running.add(id);
                             }
                             run();
                         }
                     }));
                 }
             }
             run();
         }
     };
 
     private final IRunControl.DoneGetChildren done_ctx_get_children = new IRunControl.DoneGetChildren() {
         @Override
         public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
             cmds.remove(token);
             if (error != null) {
                 errors.add(error);
             }
             else if (context_ids != null) {
                 for (String id : context_ids) {
                     cmds.add(service_rc.getContext(id, done_ctx_get_context));
                     cmds.add(service_rc.getChildren(id, this));
                 }
             }
             run();
         }
     };
 
     private final IMemory.DoneGetContext done_mem_get_context = new IMemory.DoneGetContext() {
         @Override
         public void doneGetContext(IToken token, Exception error, IMemory.MemoryContext context) {
             cmds.remove(token);
             if (error != null) {
                 errors.add(error);
             }
             else {
                 assert context != null;
                 mem_ctx = context;
                 File fnm = null;
                 try {
                     fnm = new File((String)args.get(TCFLaunchDelegate.FILES_FILE_NAME));
                     file = new RandomAccessFile(fnm, "r");
                     try {
                         downloadFile(context);
                     }
                     finally {
                         file.close();
                     }
                 }
                 catch (Exception e) {
                     if (fnm != null) e = new Exception("Cannot read '" + fnm.getName() + "'", e);
                     errors.add(e);
                 }
                 file = null;
             }
             run();
         }
     };
 
     private final IRegisters.DoneGetChildren done_regs_get_children = new IRegisters.DoneGetChildren() {
         @Override
         public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
             cmds.remove(token);
             if (error != null) errors.add(error);
             if (context_ids != null) {
                 for (String id : context_ids) {
                     cmds.add(service_regs.getContext(id, new IRegisters.DoneGetContext() {
                         @Override
                         public void doneGetContext(IToken token, Exception error, RegistersContext context) {
                             cmds.remove(token);
                             if (error != null) errors.add(error);
                             if (context != null) {
                                 if (IRegisters.ROLE_PC.equals(context.getRole())) {
                                     reg_pc = context;
                                     setEntryAddress();
                                 }
                                 else if (reg_pc == null && errors.size() == 0) {
                                     cmds.add(service_regs.getChildren(context.getID(), done_regs_get_children));
                                 }
                             }
                             run();
                         }
                     }));
                 }
             }
             run();
         }
     };
 
     private static final int PT_LOAD = 1;
 
     private boolean listener_ok;
     private boolean started_context_retrieval;
     private boolean started_reginfo_retrieval;
     private boolean disposed;
 
     private RandomAccessFile file;
     private boolean big_endian;
     private boolean elf64;
     private IMemory.MemoryContext mem_ctx;
     private BigInteger entry_addr;
     private RegistersContext reg_pc;
     private long start_time;
 
     ElfLoader(IChannel channel) {
         this.channel = channel;
         service_rc = channel.getRemoteService(IRunControl.class);
         service_mem = channel.getRemoteService(IMemory.class);
         service_regs = channel.getRemoteService(IRegisters.class);
     }
 
     void load(Map<String,Object> args, Runnable done) {
         this.args = args;
         this.done = done;
         Boolean b1 = (Boolean)args.get(TCFLaunchDelegate.FILES_DOWNLOAD);
         Boolean b2 = (Boolean)args.get(TCFLaunchDelegate.FILES_SET_PC);
         download = b1 != null && b1.booleanValue();
         set_pc = b2 != null && b2.booleanValue();
         start_time = System.currentTimeMillis();
         started_reginfo_retrieval = false;
         entry_addr = null;
         mem_ctx = null;
         reg_pc = null;
         Protocol.invokeLater(this);
     }
 
     void dispose() {
         if (service_rc != null) service_rc.removeListener(rc_listener);
         disposed = true;
     }
 
     private BigInteger readNumberX() throws IOException {
         int size = elf64 ? 8 : 4;
         byte[] buf = new byte[size + 1];
         file.readFully(buf, 1, size);
         if (!big_endian) {
             for (int i = 0; i < size / 2; i++) {
                 byte x = buf[i + 1];
                 buf[i + 1] = buf[size - i];
                 buf[size - i] = x;
             }
         }
         return new BigInteger(buf);
     }
 
     private int readInt2() throws IOException {
         int x = file.readUnsignedByte();
         int y = file.readUnsignedByte();
         return big_endian ? (x << 8) + y : x + (y << 8);
     }
 
     private int readInt4() throws IOException {
         int x = readInt2();
         int y = readInt2();
         return big_endian ? (x << 16) + y : x + (y << 16);
     }
 
     private void downloadFile(IMemory.MemoryContext context) throws Exception {
         if (file.readByte() != 0x7f || file.readByte() != 'E' ||
                 file.readByte() != 'L' || file.readByte() != 'F')
             throw new IOException("Not an ELF file");
         switch (file.readByte()) {
         case 1:
             elf64 = false;
             break;
         case 2:
             elf64 = true;
             break;
         default:
             throw new IOException("Invalid ELF file");
         }
         switch (file.readByte()) {
         case 1:
             big_endian = false;
             break;
         case 2:
             big_endian = true;
             break;
         default:
             throw new IOException("Invalid ELF file");
         }
 
         file.seek(24);
 
         entry_addr = readNumberX();
         if (download) {
             BigInteger phoff = readNumberX();
             @SuppressWarnings("unused")
             BigInteger shoff = readNumberX();
            file.skipBytes(6);
             int phentsize = readInt2();
             int phnum = readInt2();
 
             for (int n = 0; n < phnum; n++) {
                 file.seek(phoff.longValue() + n * phentsize);
                 int p_type = readInt4();
                 if (p_type != PT_LOAD) continue;
                 if (elf64) readInt4();
                 BigInteger p_offset = readNumberX();
                 @SuppressWarnings("unused")
                 BigInteger p_vaddr = readNumberX();
                 BigInteger p_paddr = readNumberX();
                 BigInteger p_filesz = readNumberX();
                 BigInteger p_memsz = readNumberX();
                 byte buf[] = new byte[p_filesz.intValue()];
                 file.seek(p_offset.longValue());
                 file.readFully(buf);
                 cmds.add(context.set(p_paddr, 4, buf, 0, buf.length, 0, new IMemory.DoneMemory() {
                     @Override
                     public void doneMemory(IToken token, IMemory.MemoryError error) {
                         cmds.remove(token);
                         if (error != null) errors.add(error);
                         run();
                     }
                 }));
                 BigInteger fill = p_memsz.subtract(p_filesz);
                 if (fill.compareTo(BigInteger.ZERO) > 0) {
                     buf = new byte[4];
                     cmds.add(context.fill(p_paddr.add(p_filesz), 4, buf, fill.intValue(), 0, new IMemory.DoneMemory() {
                         @Override
                         public void doneMemory(IToken token, IMemory.MemoryError error) {
                             cmds.remove(token);
                             if (error != null) errors.add(error);
                             run();
                         }
                     }));
                 }
             }
         }
     }
 
     private void setEntryAddress() {
         byte[] value = new byte[reg_pc.getSize()];
         boolean big_endian = reg_pc.isBigEndian();
 
         BigInteger n = entry_addr;
         for (int i = 0; i < value.length; i++) {
             value[big_endian ? value.length - i - 1 : i] = n.byteValue();
             n = n.shiftRight(8);
         }
 
         cmds.add(reg_pc.set(value, new IRegisters.DoneSet() {
             @Override
             public void doneSet(IToken token, Exception error) {
                 cmds.remove(token);
                 if (error != null) errors.add(error);
                 run();
             }
         }));
     }
 
     private String getFullName(IRunControl.RunControlContext ctx) {
         if (ctx == null) return null;
         String name = ctx.getName();
         if (name == null) name = ctx.getID();
         String parent = ctx.getParentID();
         if (parent == null) return "/" + name;
         String path = getFullName(contexts.get(parent));
         if (path == null) return null;
         return path + '/' + name;
     }
 
     public void run() {
         /* Wait for pending commands */
         if (cmds.size() > 0) return;
 
         if (disposed) return;
         if (done == null) return;
 
         if (service_rc == null || service_mem == null) {
             errors.add(new Error("No services, cannot do anything"));
         }
 
         if (!listener_ok) {
             service_rc.addListener(rc_listener);
             listener_ok = true;
         }
 
         if (errors.size() == 0 && !started_context_retrieval) {
             /* Retrieve debug context information */
             cmds.add(service_rc.getChildren(null, done_ctx_get_children));
             started_context_retrieval = true;
             return;
         }
 
         /* Suspend everything */
         if (errors.size() == 0 && running.size() > 0) {
             if (System.currentTimeMillis() - start_time < 5000) {
                 for (final String id : running) {
                     IRunControl.RunControlContext ctx = contexts.get(id);
                     if (ctx != null) {
                         cmds.add(ctx.suspend(new IRunControl.DoneCommand() {
                             @Override
                             public void doneCommand(IToken token, Exception error) {
                                 cmds.remove(token);
                                 if (error != null && running.contains(id)) errors.add(error);
                                 run();
                             }
                         }));
                     }
                 }
                 if (cmds.size() > 0) return;
             }
             errors.add(new Exception("Cannot stop the target"));
         }
 
         if (errors.size() == 0 && mem_ctx == null) {
             /* Download the file */
             String id = (String)args.get(TCFLaunchDelegate.FILES_CONTEXT_ID);
             if (id != null) {
                 cmds.add(service_mem.getContext(id, done_mem_get_context));
                 return;
             }
             String name = (String)args.get(TCFLaunchDelegate.FILES_CONTEXT_FULL_NAME);
             if (name != null) {
                 for (IRunControl.RunControlContext ctx : contexts.values()) {
                     if (name.equals(getFullName(ctx))) {
                         cmds.add(service_mem.getContext(ctx.getID(), done_mem_get_context));
                     }
                 }
                 if (cmds.size() > 0) return;
             }
             /* Wait for context */
             if (System.currentTimeMillis() - start_time < 5000) {
                 Protocol.invokeLater(200, this);
                 return;
             }
             errors.add(new Exception(
                     "Context not found: " +
                     (name != null ? name : id)));
         }
 
         if (errors.size() == 0 && mem_ctx != null && entry_addr != null &&
                 set_pc && !started_reginfo_retrieval && service_regs != null) {
             started_reginfo_retrieval = true;
             cmds.add(service_regs.getChildren(mem_ctx.getID(), done_regs_get_children));
             return;
         }
         /* All done */
         if (errors.size() > 0) channel.terminate(new Exception("Download error", errors.get(0)));
         Protocol.invokeLater(done);
         done = null;
     }
 }
