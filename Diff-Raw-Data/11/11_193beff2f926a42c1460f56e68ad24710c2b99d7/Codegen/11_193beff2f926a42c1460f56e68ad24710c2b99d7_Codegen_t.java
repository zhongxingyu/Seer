 package x86;
 
 import assem.Instr;
 import tree.Stm;
 import util.List;
 import temp.Temp;
 import tree.MaxMunch;
 
 import assem.MOVE;
 import assem.OPER;
 import assem.LABEL;
 import tree.Rest;
 
 public class Codegen
 {
     Frame frame;
     public Codegen(Frame f)
     {
         frame=f;        
     }
 
     private List<Instr> ilist=null;
     private List<Instr> last=null;
 
     private void emit(Instr inst)
     {
         if (last!=null)
             last = last.tail = new List<Instr>(inst,null);
         else 
             last = ilist = new List<Instr>(inst,null);
     }
 
     private void munchStm (Stm s) {
 		MaxMunch m = new MaxMunch(s);
		m.dump();
 		for (List<Rest> r = m.info; r != null; r = r.tail)
 		{
 			Instr i;
 			switch (r.head.type)
 			{
 				case Rest.LABEL:
 					i = new LABEL(r.head.cmd, r.head.label);
 					break;
 				case Rest.MOVE:
 					i = new MOVE(r.head.cmd, r.head.dst.head, r.head.src.head);
 					break;
 				case Rest.OPER:
 					i = new OPER(r.head.cmd, r.head.dst, r.head.src);
 					break;
 				case Rest.OPER_TARGET:
 					i = new OPER(r.head.cmd, r.head.dst, r.head.src, r.head.targets);
 					break;
 				default:
 					i = null;
 					System.out.println("Error to decide rest's type");
 					break;
 			}
 			emit(i);
 		}
 	}
 
     /*-------------------------------------------------------------*
      *                              MAIN                           *
      *-------------------------------------------------------------*/
     List<Instr> codegen(Stm s)
     {
         List<Instr> l;
         munchStm(s);
         l=ilist;
         ilist=last=null;
         return l;
     }
     
     List<Instr> codegen(List<Stm> body)
     {
         List<Instr> l = null, t = null;
         
         List<Temp> registers;
         for( ; body != null; body = body.tail )
         {
             munchStm(body.head);
             if ( l == null )
                 l = ilist;
             else
                 t.tail = ilist;
             t = last;
             ilist=last=null;
         }
         return l;
     }
 }
