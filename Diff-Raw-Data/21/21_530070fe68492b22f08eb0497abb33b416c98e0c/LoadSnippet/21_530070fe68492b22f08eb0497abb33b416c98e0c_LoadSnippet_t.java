 package piggene.serialisation.scriptcreation;
 
 import piggene.serialisation.WorkflowComponent;
 
 public class LoadSnippet extends PigSnippet {
	private static final String TXT_STORAGE = "PigStorage";
	private static final String VCF_STORAGE = "pigGene.PigGeneStorage()";

	private static final String TXT_STORAGE_TAB_SEPARATOR = "('\t')";
	private static final String TXT_STORAGE_SPACE_SEPARATOR = "(' ')";
	private static final String TXT_STORAGE_COMMA_SEPARATOR = "(',')";
 
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
		if (comp.getOptions().equals("txt")) {
			sb.append(TXT_STORAGE);
			if (comp.getOptions2().equals("tab")) {
				sb.append(TXT_STORAGE_TAB_SEPARATOR);
			} else if (comp.getOptions2().equals("space")) {
				sb.append(TXT_STORAGE_SPACE_SEPARATOR);
			} else {
				sb.append(TXT_STORAGE_COMMA_SEPARATOR);
			}
		} else {
			sb.append(VCF_STORAGE);
		}
 		return sb.toString();
 	}
 
 }
