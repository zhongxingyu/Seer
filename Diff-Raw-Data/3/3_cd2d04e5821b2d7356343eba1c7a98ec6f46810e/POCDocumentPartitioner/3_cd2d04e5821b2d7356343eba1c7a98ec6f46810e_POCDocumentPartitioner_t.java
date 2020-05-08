 package poc.document;
 
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentPartitioner;
 import org.eclipse.jface.text.ITypedRegion;
 import org.eclipse.jface.text.TypedRegion;
 
 public class POCDocumentPartitioner implements IDocumentPartitioner {
 
 	public static final String PARTITION_NAME = "MAIN";
 
 	private static final String[] types = { POCDocumentPartitioner.PARTITION_NAME };
 
 	private POCDocument document = null;
 
 	private ITypedRegion region = null;
 
 	public POCDocumentPartitioner(POCDocument document) {
 		this.document = document;
		this.region = new TypedRegion(0, this.document.getLength(), POCDocumentPartitioner.PARTITION_NAME);
 	}
 
 	/***************************************************************************
 	 * Partitioning
 	 **************************************************************************/
 
 	@Override
 	public String[] getLegalContentTypes() {
 		return POCDocumentPartitioner.types;
 	}
 
 	@Override
 	public String getContentType(int offset) {
 		return POCDocumentPartitioner.PARTITION_NAME;
 	}
 
 	@Override
 	public ITypedRegion[] computePartitioning(int offset, int length) {
 		this.region = new TypedRegion(0, this.document.getLength(), POCDocumentPartitioner.PARTITION_NAME);
 		ITypedRegion[] regions = { this.region };
 		return regions;
 	}
 
 	@Override
 	public ITypedRegion getPartition(int offset) {
 		return this.region;
 	}
 
 	/***************************************************************************
 	 * Unused
 	 **************************************************************************/
 
 	@Override public void connect(IDocument document) {}
 	@Override public void disconnect() {}
 
 	@Override
 	public void documentAboutToBeChanged(DocumentEvent event) {
		this.region = new TypedRegion(0, this.document.getLength(), POCDocumentPartitioner.PARTITION_NAME);
 	}
 
 	@Override
 	public boolean documentChanged(DocumentEvent event) {
 		return false;
 	}
 }
