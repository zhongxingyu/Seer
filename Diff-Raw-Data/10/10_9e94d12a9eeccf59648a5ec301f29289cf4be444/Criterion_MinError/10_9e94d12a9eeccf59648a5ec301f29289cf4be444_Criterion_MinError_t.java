 import java.util.ArrayList;
 
 public class Criterion_MinError implements Criterion {
 
 	@Override
 	public double calculateSplitPerf(ArrayList<Entry> lchild,
 			ArrayList<Entry> rchild, ArrayList<Entry> all) {
 		
		int type0 = Node.calculateLabel(all); 
		int errorCount0 = 0; 
		for (Entry e : all) {
			if (e.label != type0) 
				errorCount0++; 
		}
		
 		int typel = Node.calculateLabel(lchild);
 		int errorCount = 0;
 		for (Entry e : lchild) {
 			if (e.label != typel) 
 				errorCount++;
 		}
 		
 		int typer = Node.calculateLabel(rchild);
 		for (Entry e : rchild) {
 			if (e.label != typer) {
 				errorCount++;
 			}
 		}
 		
		return errorCount0 - errorCount;
 	}
 
 }
