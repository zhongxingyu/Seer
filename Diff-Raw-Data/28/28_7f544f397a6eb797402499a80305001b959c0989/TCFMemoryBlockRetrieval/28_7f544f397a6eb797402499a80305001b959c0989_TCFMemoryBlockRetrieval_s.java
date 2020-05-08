 /*******************************************************************************
  * Copyright (c) 2008, 2013 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.model;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.PlatformObject;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.IMemoryBlockManager;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.debug.core.model.IMemoryBlock;
 import org.eclipse.debug.core.model.IMemoryBlockExtension;
 import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
 import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
 import org.eclipse.debug.core.model.MemoryByte;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
 import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.debug.ui.memory.IMemoryRendering;
 import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
 import org.eclipse.debug.ui.memory.IMemoryRenderingManager;
 import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
 import org.eclipse.debug.ui.memory.IMemoryRenderingType;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.tcf.internal.debug.model.ITCFConstants;
 import org.eclipse.tcf.internal.debug.model.TCFLaunch;
 import org.eclipse.tcf.internal.debug.ui.Activator;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IExpressions;
 import org.eclipse.tcf.services.IMemory;
 import org.eclipse.tcf.services.IMemory.MemoryError;
 import org.eclipse.tcf.services.ISymbols;
 import org.eclipse.tcf.util.TCFDataCache;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * A memory block retrieval allows the user interface to request a memory block from a debugger when needed.
  * TCF memory block retrieval is based on TCF Memory service.
  */
 class TCFMemoryBlockRetrieval implements IMemoryBlockRetrievalExtension {
 
     private final TCFNodeExecContext exec_ctx;
     private final HashSet<MemoryBlock> mem_blocks = new HashSet<MemoryBlock>();
     private final LinkedList<ModelProxy> model_proxies = new LinkedList<ModelProxy>();
 
     private static class MemData {
         final BigInteger addr;
         final MemoryByte[] data;
         final byte[] bytes;
 
         MemData(BigInteger addr, MemoryByte[] data) {
             int i = 0;
             this.addr = addr;
             this.data = data;
             this.bytes = new byte[data.length];
             for (MemoryByte b : data) bytes[i++] = b.getValue();
         }
     }
 
     private class MemoryBlock extends PlatformObject implements IMemoryBlockExtension, IModelProxyFactory {
 
         private final String ctx_id;
         private final String expression;
         private final long length;
         private final Set<Object> connections = new HashSet<Object>();
         private final TCFDataCache<IExpressions.Expression> remote_expression;
         private final TCFDataCache<IExpressions.Value> expression_value;
         private final TCFDataCache<ISymbols.Symbol> expression_type;
 
         private boolean disposed;
 
         private MemData mem_data; // current memory block data
         private MemData mem_prev; // previous data - before last suspend
         private MemData mem_last; // last retrieved memory block data
 
         MemoryBlock(final String expression, long length) {
             this.ctx_id = exec_ctx.id;
             this.expression = expression;
             this.length = length;
             mem_blocks.add(this);
             final TCFLaunch launch = exec_ctx.model.getLaunch();
             final IChannel channel = launch.getChannel();
             remote_expression = new TCFDataCache<IExpressions.Expression>(channel) {
                 @Override
                 protected boolean startDataRetrieval() {
                     IExpressions exps = launch.getService(IExpressions.class);
                     if (exps == null) {
                         set(null, new Exception("Expressions service not available"), null);
                         return true;
                     }
                     command = exps.create(ctx_id, null, expression, new IExpressions.DoneCreate() {
                         public void doneCreate(IToken token, Exception error, IExpressions.Expression context) {
                             if (disposed) {
                                 IExpressions exps = channel.getRemoteService(IExpressions.class);
                                 exps.dispose(context.getID(), new IExpressions.DoneDispose() {
                                     public void doneDispose(IToken token, Exception error) {
                                         if (error == null) return;
                                         if (channel.getState() != IChannel.STATE_OPEN) return;
                                         Activator.log("Error disposing remote expression evaluator", error);
                                     }
                                 });
                                 return;
                             }
                             set(token, error, context);
                         }
                     });
                     return false;
                 }
             };
             expression_value = new TCFDataCache<IExpressions.Value>(channel) {
                 @Override
                 protected boolean startDataRetrieval() {
                     if (!remote_expression.validate(this)) return false;
                     final IExpressions.Expression ctx = remote_expression.getData();
                     if (ctx == null) {
                         set(null, null, null);
                         return true;
                     }
                     IExpressions exps = launch.getService(IExpressions.class);
                     command = exps.evaluate(ctx.getID(), new IExpressions.DoneEvaluate() {
                         public void doneEvaluate(IToken token, Exception error, IExpressions.Value value) {
                             set(token, error, value);
                         }
                     });
                     return false;
                 }
             };
             expression_type = new TCFDataCache<ISymbols.Symbol>(channel) {
                 @Override
                 protected boolean startDataRetrieval() {
                     if (!expression_value.validate(this)) return false;
                     IExpressions.Value val = expression_value.getData();
                     if (val == null) {
                         set(null, expression_value.getError(), null);
                         return true;
                     }
                     TCFDataCache<ISymbols.Symbol> type_cache = exec_ctx.model.getSymbolInfoCache(val.getTypeID());
                     if (type_cache == null) {
                         set(null, null, null);
                         return true;
                     }
                     if (!type_cache.validate(this)) return false;
                     set(null, type_cache.getError(), type_cache.getData());
                     return true;
                 }
             };
         }
 
         private void close() {
             assert Protocol.isDispatchThread();
             assert !disposed;
             disposed = true;
             expression_value.dispose();
             expression_type.dispose();
             if (remote_expression.isValid() && remote_expression.getData() != null) {
                 final IChannel channel = exec_ctx.channel;
                 if (channel.getState() == IChannel.STATE_OPEN) {
                     IExpressions exps = channel.getRemoteService(IExpressions.class);
                     exps.dispose(remote_expression.getData().getID(), new IExpressions.DoneDispose() {
                         public void doneDispose(IToken token, Exception error) {
                             if (error == null) return;
                             if (channel.getState() != IChannel.STATE_OPEN) return;
                             Activator.log("Error disposing remote expression evaluator", error);
                         }
                     });
                 }
             }
             remote_expression.dispose();
             mem_blocks.remove(MemoryBlock.this);
         }
 
         public synchronized void connect(Object client) {
             connections.add(client);
         }
 
         public synchronized void disconnect(Object client) {
             connections.remove(client);
         }
 
         public synchronized Object[] getConnections() {
             return connections.toArray(new Object[connections.size()]);
         }
 
         public void dispose() throws DebugException {
             if (disposed) return;
             new TCFDebugTask<Boolean>(exec_ctx.getChannel()) {
                 public void run() {
                     if (!disposed) close();
                     done(Boolean.TRUE);
                 }
             }.getD();
         }
 
         public int getAddressSize() throws DebugException {
             return new TCFDebugTask<Integer>(exec_ctx.getChannel()) {
                 public void run() {
                     if (exec_ctx.isDisposed()) {
                         error("Context is disposed");
                     }
                     else {
                         TCFDataCache<IMemory.MemoryContext> cache = exec_ctx.getMemoryContext();
                         if (!cache.validate(this)) return;
                         if (cache.getError() != null) {
                             error(cache.getError());
                         }
                         else {
                             IMemory.MemoryContext mem = cache.getData();
                             if (mem == null) {
                                 error("Context does not provide memory access");
                             }
                             else {
                                 done(mem.getAddressSize());
                             }
                         }
                     }
                 }
             }.getD();
         }
 
         public int getAddressableSize() throws DebugException {
             // TODO: support for addressable size other then 1 byte
             return 1;
         }
 
         public BigInteger getBigBaseAddress() throws DebugException {
             return new TCFDebugTask<BigInteger>(exec_ctx.getChannel()) {
                 public void run() {
                     if (!expression_value.validate()) {
                         expression_value.wait(this);
                     }
                     else if (expression_value.getError() != null) {
                         error(expression_value.getError());
                     }
                     else if (expression_value.getData() == null) {
                         error("Address expression evaluation failed");
                     }
                     else if (!expression_type.validate()) {
                         expression_type.wait(this);
                     }
                     else if (expression_type.getError() != null) {
                         error(expression_type.getError());
                     }
                     else {
                         IExpressions.Value value = expression_value.getData();
                         byte[] data = value.getValue();
                         if (data == null || data.length == 0) {
                             error("Address expression value is empty (void)");
                         }
                         else {
                             ISymbols.Symbol type = expression_type.getData();
                             boolean signed = type != null && type.getTypeClass() == ISymbols.TypeClass.integer;
                             done(TCFNumberFormat.toBigInteger(data, value.isBigEndian(), signed));
                         }
                     }
                 }
             }.getD();
         }
 
         public MemoryByte[] getBytesFromAddress(final BigInteger address, final long units) throws DebugException {
             return new TCFDebugTask<MemoryByte[]>(exec_ctx.getChannel()) {
                 int offs = 0;
                 public void run() {
                     if (mem_data != null &&
                             address.compareTo(mem_data.addr) >= 0 &&
                             address.add(BigInteger.valueOf(units)).compareTo(
                                     mem_data.addr.add(BigInteger.valueOf(mem_data.data.length))) <= 0) {
                         offs = address.subtract(mem_data.addr).intValue();
                         MemoryByte[] res = mem_data.data;
                         if (units < mem_data.data.length) {
                             res = new MemoryByte[(int)units];
                             System.arraycopy(mem_data.data, offs, res, 0, res.length);
                         }
                         setHistoryFlags();
                         done(res);
                         return;
                     }
                     if (exec_ctx.isDisposed()) {
                         error("Context is disposed");
                         return;
                     }
                     TCFDataCache<IMemory.MemoryContext> cache = exec_ctx.getMemoryContext();
                     if (!cache.validate(this)) return;
                     if (cache.getError() != null) {
                         error(cache.getError());
                         return;
                     }
                     final IMemory.MemoryContext mem = cache.getData();
                     if (mem == null) {
                         error("Context does not provide memory access");
                         return;
                     }
                     final int size = (int)units;
                     final int mode = IMemory.MODE_CONTINUEONERROR | IMemory.MODE_VERIFY;
                     final byte[] buf = new byte[size];
                     final MemoryByte[] res = new MemoryByte[size];
                     mem.get(address, 1, buf, 0, size, mode, new IMemory.DoneMemory() {
                         public void doneMemory(IToken token, MemoryError error) {
                             int big_endian = 0;
                             if (mem.getProperties().get(IMemory.PROP_BIG_ENDIAN) != null) {
                                 big_endian |= MemoryByte.ENDIANESS_KNOWN;
                                 if (mem.isBigEndian()) big_endian |= MemoryByte.BIG_ENDIAN;
                             }
                             int cnt = 0;
                             while (offs < size) {
                                 int flags = big_endian;
                                 if (error instanceof IMemory.ErrorOffset) {
                                     IMemory.ErrorOffset ofs = (IMemory.ErrorOffset)error;
                                     int status = ofs.getStatus(cnt);
                                     if (status == IMemory.ErrorOffset.BYTE_VALID) {
                                         flags |= MemoryByte.READABLE | MemoryByte.WRITABLE;
                                     }
                                     else if ((status & IMemory.ErrorOffset.BYTE_UNKNOWN) != 0) {
                                         if (cnt > 0) break;
                                     }
                                 }
                                 else if (error == null) {
                                     flags |= MemoryByte.READABLE | MemoryByte.WRITABLE;
                                 }
                                 res[offs] = new MemoryByte(buf[offs], (byte)flags);
                                 offs++;
                                 cnt++;
                             }
                             if (offs < size) {
                                 mem.get(address.add(BigInteger.valueOf(offs)), 1, buf, offs, size - offs, mode, this);
                             }
                             else {
                                 mem_last = mem_data = new MemData(address, res);
                                 setHistoryFlags();
                                 done(res);
                             }
                         }
                     });
                 }
             }.getD();
         }
 
         private void setHistoryFlags() {
             if (mem_data == null) return;
             BigInteger addr = mem_data.addr;
             BigInteger his_start = null;
             BigInteger his_end = null;
             if (mem_prev != null) {
                 his_start = mem_prev.addr;
                 his_end = mem_prev.addr.add(BigInteger.valueOf(mem_prev.data.length));
             }
             for (MemoryByte b : mem_data.data) {
                 int flags = b.getFlags();
                 if (mem_prev != null && addr.compareTo(his_start) >= 0 && addr.compareTo(his_end) < 0) {
                     flags |= MemoryByte.HISTORY_KNOWN;
                     int offs = addr.subtract(his_start).intValue();
                     if (b.getValue() != mem_prev.data[offs].getValue()) {
                         flags |= MemoryByte.CHANGED;
                     }
                 }
                 else {
                     flags &= ~(MemoryByte.HISTORY_KNOWN | MemoryByte.CHANGED);
                 }
                 b.setFlags((byte)flags);
                 addr = addr.add(BigInteger.valueOf(1));
             }
         }
 
         public MemoryByte[] getBytesFromOffset(BigInteger offset, long units) throws DebugException {
             return getBytesFromAddress(getBigBaseAddress().add(offset), units);
         }
 
         public String getExpression() {
             return expression;
         }
 
         public IMemoryBlockRetrieval getMemoryBlockRetrieval() {
             return TCFMemoryBlockRetrieval.this;
         }
 
         public long getStartAddress() {
             return 0; // Unbounded
         }
 
         public long getLength() {
             return length;
         }
 
         public BigInteger getMemoryBlockStartAddress() throws DebugException {
             return null; // Unbounded
         }
 
         public BigInteger getMemoryBlockEndAddress() throws DebugException {
             return null; // Unbounded
         }
 
         public BigInteger getBigLength() throws DebugException {
             return BigInteger.valueOf(length);
         }
 
         public void setBaseAddress(BigInteger address) throws DebugException {
         }
 
         public void setValue(BigInteger offset, final byte[] bytes) throws DebugException {
             final BigInteger address = getBigBaseAddress().add(offset);
             new TCFDebugTask<Object>(exec_ctx.getChannel()) {
                 public void run() {
                     if (exec_ctx.isDisposed()) {
                         error("Context is disposed");
                         return;
                     }
                     TCFDataCache<IMemory.MemoryContext> cache = exec_ctx.getMemoryContext();
                     if (!cache.validate(this)) return;
                     if (cache.getError() != null) {
                         error(cache.getError());
                         return;
                     }
                     final IMemory.MemoryContext mem = cache.getData();
                     if (mem == null) {
                         error("Context does not provide memory access");
                         return;
                     }
                     final int mode = IMemory.MODE_CONTINUEONERROR | IMemory.MODE_VERIFY;
                     mem.set(address, 1, bytes, 0, bytes.length, mode, new IMemory.DoneMemory() {
                         public void doneMemory(IToken token, MemoryError error) {
                             if (error != null) {
                                 error(error);
                             }
                             else {
                                 done(null);
                             }
                         }
                     });
                 }
             }.getD();
         }
 
         public boolean supportBaseAddressModification() throws DebugException {
             return false;
         }
 
         public boolean supportsChangeManagement() {
             return true;
         }
 
         public byte[] getBytes() throws DebugException {
             if (mem_data == null) return null;
             return mem_data.bytes;
         }
 
         public void setValue(long offset, byte[] bytes) throws DebugException {
             setValue(BigInteger.valueOf(offset), bytes);
         }
 
         public boolean supportsValueModification() {
             return true;
         }
 
         public IDebugTarget getDebugTarget() {
             return null;
         }
 
         public ILaunch getLaunch() {
             return exec_ctx.model.getLaunch();
         }
 
         public String getModelIdentifier() {
             return ITCFConstants.ID_TCF_DEBUG_MODEL;
         }
 
         public IModelProxy createModelProxy(Object element, IPresentationContext context) {
             assert element == this;
             return new ModelProxy(this, context.getWindow().getShell().getDisplay());
         }
 
         @Override
         @SuppressWarnings("rawtypes")
         public Object getAdapter(Class adapter) {
             if (adapter == IMemoryBlockRetrieval.class) return TCFMemoryBlockRetrieval.this;
             if (adapter == IMemoryBlockRetrievalExtension.class) return TCFMemoryBlockRetrieval.this;
             return super.getAdapter(adapter);
         }
     }
 
     private class ModelProxy extends AbstractModelProxy implements Runnable {
 
         final MemoryBlock mem_block;
         final Display display;
 
         ModelDelta delta;
 
         public ModelProxy(MemoryBlock mem_block, Display display) {
             this.mem_block = mem_block;
             this.display = display;
         }
 
         @Override
         public void installed(Viewer viewer) {
             synchronized (model_proxies) {
                 if (isDisposed()) return;
                 setInstalled(true);
                 super.installed(viewer);
                 model_proxies.add(this);
             }
         }
 
         @Override
         public void dispose() {
             synchronized (model_proxies) {
                 if (isDisposed()) return;
                 model_proxies.remove(this);
                 super.dispose();
             }
         }
 
         void onMemoryChanged(boolean suspended) {
             assert Protocol.isDispatchThread();
             int flags = IModelDelta.CONTENT;
             if (suspended) flags |= IModelDelta.STATE;
             if (delta != null) {
                 delta.setFlags(delta.getFlags() | flags);
             }
             else {
                 delta = new ModelDelta(mem_block, flags);
                 Protocol.invokeLater(this);
             }
         }
 
         public void run() {
             // Note: double posting is necessary to avoid deadlocks
             assert Protocol.isDispatchThread();
             final ModelDelta d = delta;
             delta = null;
             synchronized (Device.class) {
                 if (!display.isDisposed()) {
                     display.asyncExec(new Runnable() {
                         public void run() {
                             fireModelChanged(d);
                         }
                     });
                 }
             }
         }
     }
 
     TCFMemoryBlockRetrieval(TCFNodeExecContext exec_ctx) {
         this.exec_ctx = exec_ctx;
     }
 
     public IMemoryBlockExtension getExtendedMemoryBlock(final String expression, Object context) throws DebugException {
         return new TCFDebugTask<IMemoryBlockExtension>() {
             public void run() {
                 if (!exec_ctx.getMemoryContext().validate(this)) return;
                 if (exec_ctx.getMemoryContext().getError() != null) {
                     error(exec_ctx.getMemoryContext().getError());
                 }
                 else {
                     done(new MemoryBlock(expression, -1));
                 }
             }
         }.getD();
     }
 
     public IMemoryBlock getMemoryBlock(final long address, final long length) throws DebugException {
         return new TCFDebugTask<IMemoryBlockExtension>() {
             public void run() {
                 if (!exec_ctx.getMemoryContext().validate(this)) return;
                 if (exec_ctx.getMemoryContext().getError() != null) {
                     error(exec_ctx.getMemoryContext().getError());
                 }
                 else {
                     done(new MemoryBlock("0x" + Long.toHexString(address), length));
                 }
             }
         }.getD();
     }
 
     public boolean supportsStorageRetrieval() {
         return true;
     }
 
     public String getMemoryID() {
         return exec_ctx.id;
     }
 
     void flushAllCaches() {
         for (MemoryBlock b : mem_blocks) b.mem_data = null;
     }
 
     void onMemoryChanged(boolean suspended) {
         assert Protocol.isDispatchThread();
         if (mem_blocks.size() == 0) return;
         for (MemoryBlock b : mem_blocks) {
             if (suspended) b.mem_prev = b.mem_last;
             b.mem_data = null;
         }
         synchronized (model_proxies) {
             for (ModelProxy p : model_proxies) {
                 p.onMemoryChanged(suspended);
             }
         }
     }
 
     void dispose() {
         MemoryBlock[] list = mem_blocks.toArray(new MemoryBlock[mem_blocks.size()]);
         for (MemoryBlock b : list) b.close();
     }
 
     /************************** Persistence ***************************************************/
 
     private static final String
         XML_NODE_MEMORY = "Memory",
         XML_NODE_BLOCK = "Block",
         XML_NODE_RENDERING = "Rendering",
         XML_ATTR_ID = "ID",
         XML_ATTR_VIEW = "View",
         XML_ATTR_PANE = "Pane",
         XML_ATTR_CTX = "Context",
         XML_ATTR_ADDR = "Addr",
         XML_ATTR_SIZE = "Size";
 
     private static final String XML_FILE_NAME = "memview.xml";
     private static final Display display = Display.getDefault();
     private static final Map<String,List<Element>> blocks_memento = new HashMap<String,List<Element>>();
     private static final Set<Runnable> pending_updates = new HashSet<Runnable>();
 
     static boolean memento_loaded;
 
     private static void asyncExec(TCFModel model, Runnable r) {
         synchronized (pending_updates) {
             synchronized (Device.class) {
                 if (display.isDisposed()) return;
                 display.asyncExec(r);
             }
             pending_updates.add(r);
         }
     }
 
     static void onModelCreated(TCFModel model) {
         assert Protocol.isDispatchThread();
         if (memento_loaded) return;
         memento_loaded = true;
         try {
             synchronized (blocks_memento) {
                 // Load memory monitors memento from workspace
                 blocks_memento.clear();
                 IPath path = Activator.getDefault().getStateLocation();
                 File f = path.append(XML_FILE_NAME).toFile();
                 if (!f.exists()) return;
                 InputStream inp = new FileInputStream(f);
                 DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                 parser.setErrorHandler(new DefaultHandler());
                 Element xml_memory = parser.parse(inp).getDocumentElement();
                 if (xml_memory.getTagName().equals(XML_NODE_MEMORY)) {
                     Node node = xml_memory.getFirstChild();
                     while (node != null) {
                         if (node instanceof Element && ((Element)node).getTagName().equals(XML_NODE_BLOCK)) {
                             Element xml_block = (Element)node;
                             String id = xml_block.getAttribute(XML_ATTR_CTX);
                             if (id != null) {
                                 List<Element> list = blocks_memento.get(id);
                                 if (list == null) {
                                     list = new ArrayList<Element>();
                                     blocks_memento.put(id, list);
                                 }
                                 list.add(xml_block);
                             }
                         }
                         node = node.getNextSibling();
                     }
                 }
                 inp.close();
             }
         }
         catch (Exception x) {
             Activator.log("Cannot read memory monitors memento", x);
         }
     }
 
     static void onMemoryNodeCreated(TCFNodeExecContext exe_ctx) {
         assert Protocol.isDispatchThread();
         synchronized (blocks_memento) {
             // Restore memory monitors associated with the node
             final List<Element> memento = blocks_memento.remove(exe_ctx.id);
             if (memento == null || memento.size() == 0) return;
             ArrayList<IMemoryBlock> list = new ArrayList<IMemoryBlock>();
             TCFMemoryBlockRetrieval r = exe_ctx.model.getMemoryBlockRetrieval(exe_ctx);
             for (Element xml_block : memento) {
                 String expr = xml_block.getAttribute(XML_ATTR_ADDR);
                 long length = Long.parseLong(xml_block.getAttribute(XML_ATTR_SIZE));
                 list.add(r.new MemoryBlock(expr, length));
             }
             final IMemoryBlock[] blks = list.toArray(new IMemoryBlock[list.size()]);
             asyncExec(exe_ctx.model, new Runnable() {
                 @Override
                 public void run() {
                     synchronized (pending_updates) {
                         pending_updates.remove(this);
                     }
                     try {
                         int i = 0;
                         DebugPlugin.getDefault().getMemoryBlockManager().addMemoryBlocks(blks);
                         IMemoryRenderingManager rmngr = DebugUITools.getMemoryRenderingManager();
                         for (Element xml_block : memento) {
                             IMemoryBlock mb = blks[i++];
                             Node node = xml_block.getFirstChild();
                             while (node != null) {
                                 if (node instanceof Element && ((Element)node).getTagName().equals(XML_NODE_RENDERING)) {
                                     Element xml_rendering = (Element)node;
                                     String view_id = xml_rendering.getAttribute(XML_ATTR_VIEW);
                                     if (view_id != null && view_id.length() == 0) view_id = null;
                                     IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                                     IMemoryRenderingSite part = (IMemoryRenderingSite)page.showView(IDebugUIConstants.ID_MEMORY_VIEW,
                                             view_id, IWorkbenchPage.VIEW_CREATE);
                                     IMemoryRenderingType rendering_type = rmngr.getRenderingType(xml_rendering.getAttribute(XML_ATTR_ID));
                                     IMemoryRendering rendering = rendering_type.createRendering();
                                     IMemoryRenderingContainer container = part.getContainer(xml_rendering.getAttribute(XML_ATTR_PANE));
                                     rendering.init(container, mb);
                                     container.addMemoryRendering(rendering);
                                 }
                                 node = node.getNextSibling();
                             }
                         }
                     }
                     catch (Exception x) {
                         Activator.log("Cannot restore memory monitors", x);
                     }
                 }
             });
         }
     }
 
     static void onModelDisconnected(final TCFModel model) {
         assert Protocol.isDispatchThread();
         asyncExec(model, new Runnable() {
             @Override
             public void run() {
                 synchronized (pending_updates) {
                     pending_updates.remove(this);
                 }
                 // Dispose memory monitors associated with the model, update memento
                 ArrayList<IMemoryBlock> block_list = new ArrayList<IMemoryBlock>();
                 IMemoryBlockManager manager = DebugPlugin.getDefault().getMemoryBlockManager();
                 try {
                     Document document = DebugPlugin.newDocument();
                     Map<String,List<Element>> memento = new HashMap<String,List<Element>>();
                     Map<IMemoryBlock,Element> mb_to_xml = new HashMap<IMemoryBlock,Element>();
                     Element xml_memory = document.createElement(XML_NODE_MEMORY);
                     for (IMemoryBlock mb : manager.getMemoryBlocks()) {
                         if (mb instanceof MemoryBlock) {
                             MemoryBlock m = (MemoryBlock)mb;
                             TCFMemoryBlockRetrieval r = (TCFMemoryBlockRetrieval)m.getMemoryBlockRetrieval();
                             if (r.exec_ctx.model != model) continue;
                             assert r.exec_ctx.isDisposed();
                             assert m.disposed;
                             Element xml_block = document.createElement(XML_NODE_BLOCK);
                             xml_block.setAttribute(XML_ATTR_CTX, m.ctx_id);
                             xml_block.setAttribute(XML_ATTR_ADDR, m.expression);
                             xml_block.setAttribute(XML_ATTR_SIZE, Long.toString(m.length));
                             xml_memory.appendChild(xml_block);
                             mb_to_xml.put(m, xml_block);
                             List<Element> l = memento.get(m.ctx_id);
                             if (l == null) {
                                 l = new ArrayList<Element>();
                                 memento.put(m.ctx_id, l);
                             }
                             l.add(xml_block);
                             block_list.add(m);
                         }
                     }
                     for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                         for (IWorkbenchPage page : window.getPages()) {
                             String[] pane_ids = {
                                     IDebugUIConstants.ID_RENDERING_VIEW_PANE_1,
                                     IDebugUIConstants.ID_RENDERING_VIEW_PANE_2 };
                             for (IViewReference ref : page.getViewReferences()) {
                                 IViewPart part = ref.getView(false);
                                 if (part instanceof IMemoryRenderingSite) {
                                     IMemoryRenderingSite memory_view = (IMemoryRenderingSite)part;
                                     for (String pane_id : pane_ids) {
                                         IMemoryRenderingContainer container = memory_view.getContainer(pane_id);
                                         IMemoryRendering[] renderings = container.getRenderings();
                                         for (IMemoryRendering rendering : renderings) {
                                             Element xml_block = mb_to_xml.get(rendering.getMemoryBlock());
                                             if (xml_block == null) continue;
                                             Element xml_rendering = document.createElement(XML_NODE_RENDERING);
                                             xml_rendering.setAttribute(XML_ATTR_ID, rendering.getRenderingId());
                                             if (ref.getSecondaryId() != null) {
                                                 xml_rendering.setAttribute(XML_ATTR_VIEW, ref.getSecondaryId());
                                             }
                                             xml_rendering.setAttribute(XML_ATTR_PANE, pane_id);
                                             xml_block.appendChild(xml_rendering);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                     synchronized (blocks_memento) {
                         for (String id : blocks_memento.keySet()) {
                             if (memento.containsKey(id)) continue;
                             for (Element xml_block : blocks_memento.get(id)) {
                                xml_memory.appendChild(xml_block.cloneNode(true));
                             }
                         }
                         blocks_memento.clear();
                         Node node = xml_memory.getFirstChild();
                         while (node != null) {
                             if (node instanceof Element && ((Element)node).getTagName().equals(XML_NODE_BLOCK)) {
                                 Element xml_block = (Element)node;
                                 String id = xml_block.getAttribute(XML_ATTR_CTX);
                                 if (id != null) {
                                     List<Element> l = blocks_memento.get(id);
                                     if (l == null) {
                                         l = new ArrayList<Element>();
                                         blocks_memento.put(id, l);
                                     }
                                     l.add(xml_block);
                                 }
                             }
                             node = node.getNextSibling();
                         }
                         document.appendChild(xml_memory);
                         IPath path = Activator.getDefault().getStateLocation();
                         File f = path.append(XML_FILE_NAME).toFile();
                         BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
                         wr.write(DebugPlugin.serializeDocument(document));
                         wr.close();
                     }
                 }
                 catch (Exception x) {
                     Activator.log("Cannot save memory monitors", x);
                 }
                 if (block_list.size() != 0) manager.removeMemoryBlocks(block_list.toArray(new IMemoryBlock[block_list.size()]));
             }
         });
     }
 
     static void onWorkbenchShutdown() {
         // Wait until all pending updates are done
         display.syncExec(new Runnable() {
             @Override
             public void run() {
                 while (true) {
                     synchronized (pending_updates) {
                         if (pending_updates.size() == 0) return;
                     }
                     if (!display.readAndDispatch()) display.sleep();
                 }
             }
         });
     }
 }
