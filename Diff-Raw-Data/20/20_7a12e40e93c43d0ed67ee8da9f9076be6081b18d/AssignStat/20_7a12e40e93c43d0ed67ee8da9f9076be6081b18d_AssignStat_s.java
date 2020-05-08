 
 public class AssignStat extends CuStat{
 	private String var;
 	private CuExpr ee;
 	public AssignStat (String t, CuExpr e) {
 		var = t;
 		ee = e;
		super.text = var + " := " + ee.toString();
 	}
 }
