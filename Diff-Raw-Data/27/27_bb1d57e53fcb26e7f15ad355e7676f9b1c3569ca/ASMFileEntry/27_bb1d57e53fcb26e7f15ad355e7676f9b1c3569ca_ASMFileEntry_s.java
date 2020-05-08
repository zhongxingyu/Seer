 package codechicken.obfuscator.fs;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.objectweb.asm.commons.Remapper;
 
 import codechicken.lib.asm.ObfMapping;
 
 import static org.objectweb.asm.tree.AbstractInsnNode.*;
 
 public class ASMFileEntry extends ModEntry implements IRemappable
 {
     public static Map<String, Integer> types = new HashMap<String, Integer>();
     
     static
     {
         types.put("GETSTATIC", FIELD_INSN);
         types.put("PUTSTATIC", FIELD_INSN);
         types.put("GETFIELD", FIELD_INSN);
         types.put("PUTFIELD", FIELD_INSN);
         types.put("INVOKEVIRTUAL", METHOD_INSN);
         types.put("INVOKESPECIAL", METHOD_INSN);
         types.put("INVOKESTATIC", METHOD_INSN);
         types.put("INVOKEINTERFACE", METHOD_INSN);
         types.put("NEW", TYPE_INSN);
         types.put("ANEWARRAY", TYPE_INSN);
         types.put("CHECKCAST", TYPE_INSN);
         types.put("INSTANCEOF", TYPE_INSN);
         types.put("MULTIANEWARRAY", MULTIANEWARRAY_INSN);
     }
     
     public List<String> lines = new LinkedList<String>();
     public final String name;
     
     public ASMFileEntry(Mod mod, InputStream in, String name) throws IOException
     {
         super(mod);
         this.name = name;
 
         BufferedReader r = new BufferedReader(new InputStreamReader(in));
         String line;
         while((line = r.readLine()) != null)
             lines.add(line);
     }
     
     @Override
     public void remap()
     {
         Remapper mapper = mod.run.obfMapper;
         for(int i = 0; i < lines.size(); i++)
         {
             String line = lines.get(i);
             String comment = null;
             {
                 int hpos = line.indexOf('#');
                 if(hpos >= 0)
                 {
                     comment = line.substring(hpos);
                     line = line.substring(0, hpos);
                 }
             }
             
             line = line.trim();
             if(line.length() == 0) continue;
            String[] split = line.replace(" : ", ":").split(" ");
             Integer i_type = types.get(split[0]);
             if(i_type == null)
                 continue;
             
             int type = i_type;
             switch(type)
             {
                 case TYPE_INSN:
                     split[1] = ObfMapping.fromDesc(split[1]).map(mapper).s_owner;
                 break;
                 case FIELD_INSN:
                     split[1] = ObfMapping.fromDesc(split[1]).map(mapper).fieldDesc();
                 break;
                 case METHOD_INSN:
                     split[1] = ObfMapping.fromDesc(split[1]).map(mapper).methodDesc();
                 break;
                 case MULTIANEWARRAY_INSN:
                     split[1] = mapper.mapDesc(split[1]);
                 break;
             }
             
             StringBuffer sb = new StringBuffer();
             for(String s : split)
             {
                 if(sb.length() > 0)
                     sb.append(' ');
                 sb.append(s);
             }
             if(comment != null)
             {
                 sb.append(' ');
                 sb.append(comment);
             }
             lines.set(i, sb.toString());
         }
         
         mod.read.addWriteEntry(this);
     }
     
     @Override
     public String getName()
     {
         return name;
     }
     
     @Override
     public void write(OutputStream os) throws IOException
     {
         PrintWriter w = new PrintWriter(os);
         for(String line : lines)
             w.println(line);
         w.flush();
     }
 }
