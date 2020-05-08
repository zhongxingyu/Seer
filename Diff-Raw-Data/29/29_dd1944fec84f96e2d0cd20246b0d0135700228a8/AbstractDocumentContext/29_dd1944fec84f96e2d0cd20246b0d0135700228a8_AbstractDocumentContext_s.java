 package simplehtmlconverter.writer;
 
 import java.util.Stack;
 
 import simplehtmlconverter.writer.info.ParagraphInfo;
 import simplehtmlconverter.writer.info.RangeInfo;
 
 public abstract class AbstractDocumentContext implements IDocumentContext {
 	private ParagraphInfo paragraphInfo = new ParagraphInfo();
 	private RangeInfo rangeInfo = new RangeInfo();
 
 	private Stack<RangeInfo> rangeInfoStack = new Stack<RangeInfo>();
 	private Stack<ParagraphInfo> paragraphInfoStack = new Stack<ParagraphInfo>();
 
 	public void popRangeInfo() {
 		if (rangeInfoStack.size()>0) { 
 			rangeInfo=rangeInfoStack.pop();
 		}
 	}
 
 	public void pushRangeInfo() {
 		RangeInfo storedRangeInfo=null;
 		try {
 			storedRangeInfo = (RangeInfo)rangeInfo.clone();
 		} catch (CloneNotSupportedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		rangeInfoStack.push(storedRangeInfo);
 	}
 
 	public void popParagraphInfo() {
 		if (paragraphInfoStack.size()>0) { 
 			paragraphInfo=paragraphInfoStack.pop();
 		}
 	}
 
 	public void pushParagraphInfo() {
 		ParagraphInfo storedParagraphInfo=null;
 		try {
 			storedParagraphInfo = (ParagraphInfo )paragraphInfo.clone();
 		} catch (CloneNotSupportedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		paragraphInfoStack.push(storedParagraphInfo);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see builder.writer.DocumentContext#getParagraphInfo()
 	 */
 	public ParagraphInfo getParagraphInfo() {
 		return paragraphInfo;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seebuilder.writer.DocumentContext#setParagraphInfo(builder.writer.info.
 	 * ParagraphInfo)
 	 */
 	public void setParagraphInfo(ParagraphInfo paragraphInfo) {
 		this.paragraphInfo = paragraphInfo;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see builder.writer.DocumentContext#getRangeInfo()
 	 */
 	public RangeInfo getRangeInfo() {
 		return rangeInfo;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * builder.writer.DocumentContext#setRangeInfo(builder.writer.info.RangeInfo
 	 * )
 	 */
 	public void setRangeInfo(RangeInfo rangeInfo) {
 		this.rangeInfo = rangeInfo;
 	}
 
 	public abstract IDocumentWriter getDocumentWriter();
 }
