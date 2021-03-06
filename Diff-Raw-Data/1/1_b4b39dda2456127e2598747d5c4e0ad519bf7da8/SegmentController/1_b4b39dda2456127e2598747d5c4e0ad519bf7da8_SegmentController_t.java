 package com.spartansoftwareinc.vistatec.rwb.segment;
 
 import com.spartansoftwareinc.vistatec.rwb.its.LanguageQualityIssue;
 import com.spartansoftwareinc.vistatec.rwb.its.Provenance;
 import com.spartansoftwareinc.vistatec.rwb.rules.RuleConfiguration;
 import com.spartansoftwareinc.vistatec.rwb.segment.okapi.HTML5Parser;
 import com.spartansoftwareinc.vistatec.rwb.segment.okapi.HTML5Writer;
 import com.spartansoftwareinc.vistatec.rwb.segment.okapi.OkapiSegmentWriter;
 import com.spartansoftwareinc.vistatec.rwb.segment.okapi.XLIFFParser;
 import com.spartansoftwareinc.vistatec.rwb.segment.okapi.XLIFFWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 /**
  * Class for handling events related to segments, such as parsing/updating the
  * data, writing out the segments, refreshing their table view, etc.
  */
 public class SegmentController {
     private SegmentTableModel segmentModel;
     private SegmentView segmentView;
     private OkapiSegmentWriter segmentWriter;
     private XLIFFParser xliffParser;
     private HTML5Parser html5Parser;
     private boolean openFile = false, isHTML;
 
     public SegmentController() {
         this.segmentModel = new SegmentTableModel(this);
     }
 
     public void setSegmentView(SegmentView segView) {
         this.segmentView = segView;
     }
 
     public boolean isHTML() {
         return isHTML;
     }
 
     public void setHTML(boolean isHTML) {
         this.isHTML = isHTML;
     }
 
     /**
      * Check if a file has been opened by the workbench.
      */
     public boolean openFile() {
         return this.openFile;
     }
 
     public void setOpenFile(boolean openFile) {
         this.openFile = openFile;
     }
 
     /**
      * For setting the JTable and TableRowSorter models. Also used in PluginManagerView (TODO: Change).
      */
     public SegmentTableModel getSegmentTableModel() {
         return this.segmentModel;
     }
 
     protected Segment getSegment(int row) {
         return getSegmentTableModel().getSegment(row);
     }
 
     protected int getNumSegments() {
         return getSegmentTableModel().getRowCount();
     }
 
     protected int getSegmentNumColumnIndex() {
         return getSegmentTableModel().getColumnIndex(SegmentTableModel.COLSEGNUM);
     }
 
     protected int getSegmentSourceColumnIndex() {
         return getSegmentTableModel().getColumnIndex(SegmentTableModel.COLSEGSRC);
     }
 
     protected int getSegmentTargetColumnIndex() {
         return getSegmentTableModel().getColumnIndex(SegmentTableModel.COLSEGTGT);
     }
 
     protected int getSegmentTargetOriginalColumnIndex() {
         return getSegmentTableModel().getColumnIndex(SegmentTableModel.COLSEGTGTORI);
     }
 
     protected void fireTableDataChanged() {
         getSegmentTableModel().fireTableDataChanged();
        segmentView.updateRowHeights();
     }
 
     public void notifyAddedLQI(LanguageQualityIssue lqi, Segment seg) {
         segmentView.notifyAddedLQI(lqi, seg);
     }
 
     public void notifyModifiedLQI(LanguageQualityIssue lqi, Segment seg) {
         updateSegment(seg);
         segmentView.notifyModifiedLQI(lqi, seg);
     }
 
     public void notifyAddedProv(Provenance prov) {
         segmentView.notifyAddedProv(prov);
     }
 
     public void notifyDeletedSegments() {
         segmentView.notifyDeletedSegments();
     }
 
     public RuleConfiguration getRuleConfig() {
         return segmentView.ruleConfig;
     }
 
     public void parseXLIFFFile(File xliffFile) throws FileNotFoundException {
         segmentView.clearTable();
         getSegmentTableModel().deleteSegments();
 
         xliffParser = new XLIFFParser(this);
         xliffParser.parseXLIFFFile(new FileInputStream(xliffFile), segmentModel);
         setOpenFile(true);
         setHTML(false);
         segmentWriter = new XLIFFWriter(xliffParser);
         segmentView.reloadTable();
     }
 
     public void parseHTML5Files(File srcHTMLFile, File tgtHTMLFile) throws FileNotFoundException {
         segmentView.clearTable();
         getSegmentTableModel().deleteSegments();
 
         html5Parser = new HTML5Parser(this);
         html5Parser.parseHTML5Files(new FileInputStream(srcHTMLFile),
                 new FileInputStream(tgtHTMLFile));
         setOpenFile(true);
         setHTML(true);
         segmentWriter = new HTML5Writer(html5Parser);
         segmentView.reloadTable();
     }
 
     public void addSegment(Segment seg) {
         getSegmentTableModel().addSegment(seg);
     }
 
     public void updateSegment(Segment seg) {
         segmentWriter.updateEvent(seg, this);
     }
 
     public String getFileSourceLang() {
         return isHTML() ? html5Parser.getSourceLang() : xliffParser.getSourceLang();
     }
 
     public String getFileTargetLang() {
         return isHTML() ? html5Parser.getTargetLang() : xliffParser.getTargetLang();
     }
 
     /**
      * Save the XLIFF file to the file system.
      * @param file
      * @throws UnsupportedEncodingException
      * @throws FileNotFoundException
      * @throws IOException 
      */
     public void save(File file) throws UnsupportedEncodingException, FileNotFoundException, IOException {
         XLIFFWriter okapiXLIFFWriter = (XLIFFWriter) segmentWriter;
         okapiXLIFFWriter.save(file);
     }
 
     /**
      * Save the aligned HTML5 source and target files to the file system.
      * @param source
      * @param target
      * @throws UnsupportedEncodingException
      * @throws FileNotFoundException
      * @throws IOException 
      */
     public void save(File source, File target) throws UnsupportedEncodingException, FileNotFoundException, IOException {
         HTML5Writer okapiHTML5Writer = (HTML5Writer) segmentWriter;
         okapiHTML5Writer.save(source, target);
     }
 }
