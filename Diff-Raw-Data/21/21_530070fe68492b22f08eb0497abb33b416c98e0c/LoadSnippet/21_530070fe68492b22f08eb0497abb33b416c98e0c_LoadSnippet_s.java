 package piggene.serialisation.scriptcreation;
 
 import piggene.serialisation.WorkflowComponent;
 
 public class LoadSnippet extends PigSnippet {
	private static final String STORAGE = "pigGene.PigGeneStorage()";
 
 	public LoadSnippet(final WorkflowComponent comp) {
 		super(comp);
 	}
 
 	@Override
 	public String toPigScript() {
 		final StringBuilder sb = new StringBuilder();
 		sb.append(comp.getName());
 		sb.append(EQUAL_SYMBOL);
 		sb.append(comp.getOperation());
 		sb.append(" '$");
 		sb.append(comp.getRelation());
 		sb.append("' ");
 		sb.append("USING ");
		sb.append(STORAGE);
 		return sb.toString();
 	}
 
 }
