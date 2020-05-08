 package uk.ac.ebi.esd.client.ui.module;
 
 import uk.ac.ebi.esd.client.LinkManager;
 import uk.ac.ebi.esd.client.query.ObjectReport;
 import uk.ac.ebi.esd.client.query.Report;
 import uk.ac.ebi.esd.client.shared.AttributeReport;
 import uk.ac.ebi.esd.client.ui.ResultRenderer;
 
 import com.smartgwt.client.data.DataSource;
 import com.smartgwt.client.data.Record;
 import com.smartgwt.client.data.fields.DataSourceIntegerField;
 import com.smartgwt.client.data.fields.DataSourceTextField;
 import com.smartgwt.client.types.ExpansionMode;
 import com.smartgwt.client.types.Overflow;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.grid.ListGridField;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 import com.smartgwt.client.widgets.grid.events.RecordCollapseEvent;
 import com.smartgwt.client.widgets.grid.events.RecordCollapseHandler;
 import com.smartgwt.client.widgets.layout.VLayout;
 
 public class ResultPane extends VLayout implements ResultRenderer
 {
 public final static int MAX_GROUPS_PER_PAGE=30;
 public final static int MAX_SAMPLES_PER_PAGE=30;
  
  private String query;
  private boolean searchSamples;
  private boolean searchGroups;
  private boolean searchAtNames;
  private boolean searchAtValues;
  
  private ListGrid resultGrid = new GroupsList();
  private PagingRuler pagingRuler = new PagingRuler("groupPage");
  
  public ResultPane()
  {
   setHeight("100%");
   
   setMargin(5);
   
   setShowEdges(true);
   setEdgeSize(6);
   setEdgeImage("gnframe.gif");
   setEdgeMarginSize(10);
   
   resultGrid.setShowAllRecords(true);  
   resultGrid.setWrapCells(true);
   resultGrid.setFixedRecordHeights(false);
   resultGrid.setCellHeight(20);
   
   resultGrid.setBodyOverflow(Overflow.VISIBLE);
   resultGrid.setOverflow(Overflow.VISIBLE);
   
   resultGrid.setStyleName("reportGrid");
   
 //  setBaseStyle("reportGrid");
   
   DataSource ds = new DataSource();
   
   ds.setClientOnly(true);
   
   ds.addField(new DataSourceTextField("id", "ID"));
   ds.addField(new DataSourceTextField("sampCnt", "Samples", 100));
   ds.addField(new DataSourceTextField("desc", "Description", 2000));
   ds.addField(new DataSourceIntegerField("prop", "Property", 111));
   
 //  setDataSource(ds);
   
   resultGrid.setCanExpandRecords(true); 
   
   ListGridField idField = new ListGridField("id","ID", 280);  
   ListGridField sCntField = new ListGridField("sampCnt","Samples",60);
   ListGridField descField = new ListGridField("desc","Description");
 //  ListGridField propField = new ListGridField("prop","AdditionalProp");
   
 //  propField.setHidden(true);
   
 //  idField.setWidth(200);
      
   resultGrid.setFields(idField,sCntField, descField );
   resultGrid.setExpansionMode(ExpansionMode.DETAIL_FIELD);
   
   pagingRuler.setVisible(false);
   
   addMember(pagingRuler);
   addMember(resultGrid);
   
   resultGrid.addRecordCollapseHandler( new RecordCollapseHandler()
   {
    
    @Override
    public void onRecordCollapse(RecordCollapseEvent event)
    {
     LinkManager.getInstance().removeLinkClickListener(event.getRecord().getAttribute("id"));
    }
   });
 //  ListGridRecord rec = new ListGridRecord();
 //  
 //  rec.setAttribute("id", "SBM00000X");
 //  rec.setAttribute("desc", "This is my first submission description\nline 2 This is my first submission description\\nline 2This is my first submission description\\nline 2This is my first submission description\\nline 2This is my first submission description\\nline 2");
 //  rec.setAttribute("prop", 100);
 //  
 //  addData(rec);
   
 //  rec = new ListGridRecord();
 //  
 //  rec.setAttribute("id", "SBM00000X");
 //  rec.setAttribute("desc", "This is my first submission description");
 //  rec.setAttribute("prop", 100);
 //
 //  ds.addData(rec);
   
 //  rec.setDetailDS(ds);
   
 //  setAlign(Alignment.CENTER);
 //  
 //  Label lb = new Label("Query result is empty");
 //  
 //  lb.setAlign(Alignment.CENTER);
 //  
 //  addMember(lb);
  }
  
  public void showResult( Report res, String qry, boolean sSmp, boolean sGrp, boolean sAtrNm, boolean sAtrVl, int cpage )
  {
   query = qry;
   searchSamples=sSmp;
   searchGroups=sGrp;
   searchAtNames=sAtrNm;
   searchAtValues=sAtrVl;
   
   resultGrid.selectAllRecords();
   resultGrid.removeSelectedData();
   
   if( res.getTotalRecords() > MAX_GROUPS_PER_PAGE )
   {
    pagingRuler.setPagination(cpage, res.getTotalRecords(), MAX_GROUPS_PER_PAGE);
    pagingRuler.setVisible(true);
   }
   else
    pagingRuler.setVisible(false);
   
   for( ObjectReport sgr : res.getObjects() )
   {
    ListGridRecord rec = new ListGridRecord();
    
    rec.setAttribute("id", sgr.getId());
    rec.setAttribute("sampCnt", sgr.getRefCount());
    rec.setAttribute("desc", sgr.getDescription());
 
    Record det = new ListGridRecord();
    
    for( AttributeReport ar : sgr.getAttributes() )
    {
 //    System.out.println("Attr: "+ar.getName()+" "+ar.getValue());
     det.setAttribute( ar.getName(), ar.getValue() );
    }
   
    det.setAttribute("Total/matched samples", String.valueOf(sgr.getRefCount())+"/"+sgr.getMatchedCount());
 //   det.setAttribute("Selected samples", sgr.getMatchedCount());
   
    rec.setAttribute("details", new Record[]{det});
    
    resultGrid.addData(rec);
   }
  }
 
 
  
 // private void renderResultList(final VLayout lay, final Report smpls)
 // {
 //  if( smpls.getObjects().size() < 50 )
 //  {
 //   renderSampleList(lay,smpls);
 //   return;
 //  }
 //  
 //  final Window waitW = new Window();
 //  
 //  waitW.setHeight(100);
 //  waitW.setWidth(250);
 //  waitW.setShowMinimizeButton(false);  
 //  waitW.setIsModal(true);  
 //  waitW.setShowModalMask(true);  
 //  waitW.centerInPage();
 //
 //  Label msg = new Label("<b>Please wait for result rendering</b>");
 //  msg.setAlign(Alignment.CENTER);
 //  
 //  waitW.addItem(msg);
 //  
 //  waitW.addDrawHandler(new DrawHandler()
 //  {
 //   @Override
 //   public void onDraw(DrawEvent event)
 //   {
 //    DeferredCommand.addCommand(new Command()
 //    {
 //     @Override
 //     public void execute()
 //     {
 //      renderSampleList(lay,smpls);
 //      waitW.destroy();
 //     }
 //    });
 //   }
 //  });
 //  
 //  waitW.show();
 //  
 // }
 
  
  
  private class GroupsList extends ListGrid
  {
   protected Canvas getExpansionComponent(final ListGridRecord record)
   {
    return new GroupDetailsPanel(record.getAttributeAsRecordArray("details")[0], query, searchAtNames, searchAtValues );
   }
  }
  
 }
