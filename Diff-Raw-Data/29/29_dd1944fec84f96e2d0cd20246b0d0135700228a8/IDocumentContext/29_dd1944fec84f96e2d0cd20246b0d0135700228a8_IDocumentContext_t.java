 package simplehtmlconverter.writer;
 
 import simplehtmlconverter.writer.info.ParagraphInfo;
 import simplehtmlconverter.writer.info.RangeInfo;
 
 public interface IDocumentContext {
 
 	public abstract ParagraphInfo getParagraphInfo();
 
 	public abstract void setParagraphInfo(ParagraphInfo paragraphInfo);
 
 	public abstract RangeInfo getRangeInfo();
 
 	public abstract void setRangeInfo(RangeInfo rangeInfo);
 
 	public abstract IDocumentWriter getDocumentWriter();
 
 	public void popRangeInfo();
 	public void pushRangeInfo();
 
 	public void popParagraphInfo();
 	public void pushParagraphInfo();
	
	public void setHasActiveParagraph(boolean hasActiveParagraph);
	public boolean hasActiveParagraph();
 }
