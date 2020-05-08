 package uk.ac.ebi.biosd.client.ui.module;
 
 import java.util.List;
 
 import uk.ac.ebi.biosd.client.LinkClickListener;
 import uk.ac.ebi.biosd.client.LinkManager;
 import uk.ac.ebi.biosd.client.QueryService;
 import uk.ac.ebi.biosd.client.query.AttributedImprint;
 import uk.ac.ebi.biosd.client.query.AttributedObject;
 import uk.ac.ebi.biosd.client.query.SampleList;
 import uk.ac.ebi.biosd.client.shared.AttributeClassReport;
 import uk.ac.ebi.biosd.client.shared.AttributeReport;
 import uk.ac.ebi.biosd.client.shared.Pair;
 import uk.ac.ebi.biosd.client.ui.SampleListGrid;
 import uk.ac.ebi.biosd.client.ui.SourceIconBundle;
 import uk.ac.ebi.biosd.client.ui.test.ObjectViewer;
 
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.smartgwt.client.data.DataSource;
 import com.smartgwt.client.data.Record;
 import com.smartgwt.client.data.fields.DataSourceTextField;
 import com.smartgwt.client.types.Overflow;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.form.DynamicForm;
 import com.smartgwt.client.widgets.form.fields.LinkItem;
 import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
 import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
 import com.smartgwt.client.widgets.grid.HoverCustomizer;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.grid.ListGridField;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 import com.smartgwt.client.widgets.grid.events.CellClickEvent;
 import com.smartgwt.client.widgets.grid.events.CellClickHandler;
 import com.smartgwt.client.widgets.layout.VLayout;
 import com.smartgwt.client.widgets.viewer.DetailViewer;
 
 public class GroupDetailsPanel extends VLayout
 {
  private static final int BRIEF_LEN=100;
  
  private boolean allSamples;
  private String groupID;
  
  private String query;
  private boolean searchAtNames;
  private boolean searchAtValues;
  
  private List< Pair<String, String> > otherInfoList;
  private List< AttributedImprint > publList;
  private List< AttributedImprint > contList;
  
  private PagingRuler pager;
  
  @SuppressWarnings("unchecked")
  public GroupDetailsPanel(final Record r, final String query, final boolean searchAtNames, final boolean searchAtValues)
  {
   this.query=query;
   this.searchAtNames=searchAtNames;
   this.searchAtValues=searchAtValues;
   
   DataSource ds = new DataSource();
   
   ds.setClientOnly(true);
   
   ListGridRecord rec = new ListGridRecord();
 
   for( String s : r.getAttributes() )
   {
    if( s.startsWith("__")  )
     continue;
    
    ds.addField(new DataSourceTextField(s, s));
    
    String val = r.getAttributeAsString(s);
    
    if("Link".equals(s) )
    {
     String dsAttr = r.getAttributeAsString("Data Source");
     
     if( dsAttr != null )
     {
      String icon = SourceIconBundle.getIcon(dsAttr);
      
      if( icon != null )
       rec.setAttribute(s, "<a target='_blank' border=0 href='"+val+"'><img border=0 src='"+icon+"'></a>");
      else
       rec.setAttribute(s, "<a target='_blank' border=0 href='"+val+"'>"+val+"</a>");
 //     
 //     if("Array Express".equals(dsAttr))
 //      rec.setAttribute(s, "<a target='_blank' border=0 href='"+val+"'><img border=0 src='images/ae.png'></a>");
 //     else if("Pride".equals(dsAttr))
 //      rec.setAttribute(s, "<a target='_blank' border=0 href='"+val+"'><img border=0 src='images/pride.jpg'></a>");
 //     else
 //      rec.setAttribute(s, "<a target='_blank' border=0 href='"+val+"'>"+val+"</a>");
     }
     else
      rec.setAttribute(s, "<a target='_blank' border=0 href='"+val+"'>"+val+"</a>");
     
    }
    else if("PubMed ID".equals(s))
     rec.setAttribute(s, "<a target='_blank' border=0 href='http://www.ncbi.nlm.nih.gov/pubmed/"+val+"'>"+val+"</a>");
    else if("DOI".equals(s))
     rec.setAttribute(s, "<a target='_blank' border=0 href='http://dx.doi.org/"+val+"'>"+val+"</a>");
    else
     rec.setAttribute(s, val);
 
    //   System.out.println("At: "+s+" "+r.getAttributeAsString(s));
   }
 
   groupID = r.getAttributeAsString("ID");
   
   publList = (List< AttributedImprint >) r.getAttributeAsObject("__publ");
   
   if( publList != null && publList.size() > 0 )
   {
    String repstr = makeRepresentationString(publList, "publ");
 
    ds.addField(new DataSourceTextField("publ", "Publications") );
    
    rec.setAttribute("publ", repstr);
   }
   
   contList = (List< AttributedImprint >) r.getAttributeAsObject("__contact");
   
   if( contList != null && contList.size() > 0 )
   {
    String repstr = makeRepresentationString(contList, "contact");
 
    ds.addField(new DataSourceTextField("contact", "Contacts") );
    
    rec.setAttribute("contact", repstr);
   }
 
   
   otherInfoList = (List< Pair<String, String> >) r.getAttributeAsObject("__other");
   
   if( otherInfoList != null && otherInfoList.size() > 0 )
   {
    String repstr = "";
    int lastBold = -1;
    
    for( Pair<String, String> fstEl : otherInfoList )
    {
     if( repstr.length() > BRIEF_LEN )
      break;
     
     repstr += "<b>"+fstEl.getFirst()+"</b>"; 
     
     lastBold=repstr.length();
     
     repstr += ": "+fstEl.getSecond()+"; ";
    }
 
    if( repstr.length() > BRIEF_LEN )
     repstr=repstr.substring(0,BRIEF_LEN);
    
    if( BRIEF_LEN < lastBold )
     repstr+="</b>";
    
 //   Pair<String, String> fstEl = otherInfoList.get(0);
 //   String repstr = "<b>"+fstEl.getFirst()+"</b>";
 //   
 //   if( repstr.length() > BRIEF_LEN )
 //    repstr.substring(0,BRIEF_LEN);
 //   else
 //    repstr += ": "+fstEl.getSecond();
 // 
 //   if( repstr.length() > BRIEF_LEN )
 //    repstr=repstr.substring(0,BRIEF_LEN);
    
    repstr += "... <a class='el' href='javascript:linkClicked(&quot;"+groupID+"&quot;,&quot;other&quot;)'>more</a>";
    
    ds.addField(new DataSourceTextField("Other", "Other") );
    
    rec.setAttribute("Other", repstr);
   }
    
   
  
   String summ = r.getAttributeAsString("__summary");
   if( summ != null )
   {
    ds.addField(new DataSourceTextField("__smm", "Total/matched samples"));
 
    rec.setAttribute("__smm", summ);
   }
   
   ds.addData(rec);
   
   
   DetailViewer dv = new DetailViewer();
   dv.setWidth("90%");
   dv.setDataSource(ds);
   dv.setStyleName("groupDetails");
   
   dv.setAutoFetchData(true);
   
   Canvas spc = new Canvas();
   spc.setHeight(15);
 
   addMember(spc);
   
   addMember(dv);
   
   
   DynamicForm lnkform = new DynamicForm();
   lnkform.setWidth(500);
   
   lnkform.setNumCols(6);
   
   LinkItem li = new LinkItem("allsamples");
   li.setTitle("Show");
   li.setLinkTitle("all samples");
  
   li.addClickHandler( new ClickHandler()
   {
    @Override
    public void onClick(ClickEvent event)
    {
     showAllSamples(1);
    }
   });
   
   LinkItem li2 = new LinkItem("selsamples");
   li2.setTitle("Show");
   li2.setLinkTitle("matched samples");
   
   li2.addClickHandler( new ClickHandler()
   {
    @Override
    public void onClick(ClickEvent event)
    {
     showMatchedSamples( 1 );
    }
   });
 
   LinkItem li3 = new LinkItem("hidesamples");
   li3.setTitle("Hide");
   li3.setLinkTitle("samples");
   
   li3.addClickHandler( new ClickHandler()
   {
    @Override
    public void onClick(ClickEvent event)
    {
     Canvas[] membs = getMembers();
     
     if( membs[membs.length-1] instanceof ListGrid )
      removeMember(membs[membs.length-1]);
     
     pager.setVisible(false);
    }
   });
 
   lnkform.setFields( li, li2, li3 );
   
   addMember(lnkform);
   
   spc = new Canvas();
  spc.setHeight(15);
   addMember(spc);
 
   pager = new PagingRuler(groupID);
   pager.setWidth("99%");
   pager.setVisible(false);
   addMember(pager);
   
   
   LinkManager.getInstance().addLinkClickListener(groupID, new LinkClickListener()
   {
    @Override
    public void linkClicked(String param)
    {
     if( "other".equals(param) )
     {
      new NameValuePanel( otherInfoList ).show();
      return;
     }
     else if( "publ".equals(param) )
     {
      new AttributedListPanel( "Publications", publList ).show();
      return;
     }
     else if( "contact".equals(param) )
     {
      new AttributedListPanel( "Contacts", contList ).show();
      return;
     }
 
     
     int pNum = 1;
     
     try
     {
      pNum=Integer.parseInt(param);
     }
     catch(Exception e)
     {
     }
     
     if( allSamples )
      showAllSamples(pNum);
     else
      showMatchedSamples(pNum);
    }
   });
  }
  
  private String makeRepresentationString( List< AttributedImprint > list, String theme )
  {
   AttributedImprint fstEl = list.get(0);
   String repstr = "";
   
   int lastBold = -1;
   
   for( AttributeReport attr : fstEl.getAttributes() )
   {
    if( repstr.length() > BRIEF_LEN )
     break;
    
    repstr += "<b>"+attr.getName()+"</b>"; 
    
    lastBold=repstr.length();
    
    repstr += ": "+attr.getValue()+"; ";
   }
 
   if( repstr.length() > BRIEF_LEN )
    repstr=repstr.substring(0,BRIEF_LEN);
   
   if( BRIEF_LEN < lastBold )
    repstr+="</b>";
  
   repstr += "... <a class='el' href='javascript:linkClicked(&quot;"+groupID+"&quot;,&quot;"+theme+"&quot;)'>more</a>";
   
   return repstr;
  }
  
  private void showAllSamples( final int pNum )
  {
   allSamples=true;
   
   WaitWindow.showWait();
   
   QueryService.Util.getInstance().getSamplesByGroup(groupID,(pNum-1)*ResultPane.MAX_SAMPLES_PER_PAGE, ResultPane.MAX_SAMPLES_PER_PAGE,
    new AsyncCallback<SampleList>(){
 
    @Override
    public void onFailure(Throwable arg0)
    {
     WaitWindow.hideWait();
     arg0.printStackTrace();
    }
 
    @Override
    public void onSuccess(final SampleList arg0)
    {
     Scheduler.get().scheduleDeferred(new ScheduledCommand()
     {
      @Override
      public void execute()
      {
       renderSampleList2(GroupDetailsPanel.this,arg0,pNum);
       WaitWindow.hideWait();
      }
     });
     
 //    DeferredCommand.addCommand(new Command()
 //    {
 //     @Override
 //     public void execute()
 //     {
 //      renderSampleList(GroupDetailsPanel.this,arg0,pNum);
 //      WaitWindow.hideWait();
 //     }
 //    });
    }
   });
  }
  
  private void showMatchedSamples( final int pNum )
  {
   allSamples=false;
   
   WaitWindow.showWait();
   QueryService.Util.getInstance().getSamplesByGroupAndQuery(groupID, query, searchAtNames, searchAtValues,(pNum-1)*ResultPane.MAX_SAMPLES_PER_PAGE, ResultPane.MAX_SAMPLES_PER_PAGE,
    
   new AsyncCallback<SampleList>(){
 
    @Override
    public void onFailure(Throwable arg0)
    {
     arg0.printStackTrace();
     WaitWindow.hideWait();
    }
 
    @Override
    public void onSuccess( final SampleList rep )
    {
     renderSampleList2(GroupDetailsPanel.this,rep, pNum);
     WaitWindow.hideWait();
    }
   });
  }
 
  /*
  private void renderSampleList(final VLayout lay, Report smpls, int pg)
  {
   
   DataSource ds = new DataSource();
   ds.setClientOnly(true);
 
   ListGridRecord[] records = new ListGridRecord[smpls.getObjects().size()];
 
   int rc = 0;
 
   Map<String, AttributeFieldInfo> hdr = new TreeMap<String, AttributeFieldInfo>();
 
   Set<String> localHdr = new TreeSet<String>();
 
   for(GroupImprint o : smpls.getObjects())
   {
    localHdr.clear();
 
    ListGridRecord rec = new ListGridRecord();
 
    for(AttributeReport at : o.getAttributes())
    {
     String fname = null;
     int i = 0;
     while(true)
     {
      fname = at.getName() + "#" + (at.isCustom() ? "C" : "D") + "#" + i;
 
      if(!localHdr.contains(fname))
       break;
      
      i++;
     }
 
     localHdr.add(fname);
 
     AttributeFieldInfo h = hdr.get(fname);
 
     if(h == null)
     {
      hdr.put(fname, h = new AttributeFieldInfo(at.isCustom()?at.getName():"<b>"+at.getName()+"</b>", fname, at.getOrder()));
      h.add(at.getOrder());
     }
      
 //    else
 //     h.add(at.getOrder());
 
     rec.setAttribute(fname, at.getValue());
    }
 
    records[rc++] = rec;
   }
 
   ArrayList<AttributeFieldInfo> hlist = new ArrayList<AttributeFieldInfo>(hdr.size());
   hlist.addAll(hdr.values());
   Collections.sort(hlist);
 
   if( smpls.getTotalGroups() > ResultPane.MAX_SAMPLES_PER_PAGE )
   {
    pager.setContents(pg, smpls.getTotalGroups(),  ResultPane.MAX_SAMPLES_PER_PAGE, null, null );
    pager.setVisible(true);
   }
   else
    pager.setVisible(false);
   
   ListGrid attrList = new SampleListGrid();
   
   attrList.setShowAllRecords(true);
   attrList.setShowRowNumbers(true); 
   
   attrList.setWidth("99%");
   attrList.setHeight(1);
   attrList.setBodyOverflow(Overflow.VISIBLE);
   attrList.setOverflow(Overflow.VISIBLE);
   attrList.setLeaveScrollbarGap(false);
   attrList.setStyleName("sampleGrid");
   attrList.setMargin(10);
   attrList.setHoverWidth(200);
   attrList.setShowEdges(true);
 
   final ListGridField[] lfl = new ListGridField[hlist.size()];
 
   for(int i = 0; i < lfl.length; i++)
   {
    lfl[i] = new ListGridField(hlist.get(i).getField(), hlist.get(i).getTitle());
    lfl[i].setShowHover(true);
   }
   
   attrList.setHoverCustomizer(new HoverCustomizer()
   {
    @Override
    public String hoverHTML(Object value, ListGridRecord record, int rowNum, int colNum)
    {
     if( colNum == 0)
      return null;
     
     return record.getAttributeAsString(lfl[colNum-1].getName());
    }
   });
   
   if( lfl.length > 0 )
   {
    attrList.setFields(lfl);
    attrList.setData(records);
   }
   else
    attrList.setFields(new ListGridField("ID","ID"));
   
   Canvas[] membs = lay.getMembers();
   
   if( membs[membs.length-1] instanceof ListGrid )
    lay.removeMember(membs[membs.length-1]);
 
   
   lay.addMember(attrList);
 
  }
 
  */
  
  private void renderSampleList2(final VLayout lay, SampleList smpls, int pg)
  {
   
 //  DataSource ds = new DataSource();
 //  ds.setClientOnly(true);
 
   ListGrid attrList = new SampleListGrid();
   
   attrList.setShowAllRecords(true);
   attrList.setShowRowNumbers(true); 
   
   attrList.setWidth("99%");
   attrList.setHeight(1);
   attrList.setBodyOverflow(Overflow.VISIBLE);
   attrList.setOverflow(Overflow.VISIBLE);
   attrList.setLeaveScrollbarGap(false);
   attrList.setStyleName("sampleGrid");
  attrList.setMargin(10);
   attrList.setHoverWidth(200);
   attrList.setShowEdges(true);
   
 //  DataSource ds = new DataSource();
 //  ds.setClientOnly(true);
 //
 //  attrList.setDataSource(ds);
 //  attrList.setShowHover(true);
   
 //  attrList.setAutoFetchData(true);
 
 //  for( AttributeClassReport cls : smpls.getHeader() )
 //  {
 //   DataSourceTextField dsf = new DataSourceTextField(cls.getId(), cls.isCustom()?cls.getName():("<b>"+cls.getName()+"</b>"));
 //   
 //   if( cls.getId().equals("__id") )
 //    dsf.setPrimaryKey(true);
 //   
 //   ds.addField(dsf );
 //  }
 
   final ListGridField[] lfl = new ListGridField[smpls.getHeader().size()];
 
   String prideKey = null;
   
   final List<AttributeClassReport> header = smpls.getHeader();
   
   int i=0;
   for( AttributeClassReport cls : header )
   {
    lfl[i] = new ListGridField(cls.getId(), cls.isCustom()?cls.getName():("<b>"+cls.getName()+"</b>") );
    lfl[i].setShowHover(true);
    
    if( cls.getName().equals("Pride ID") )
     prideKey = cls.getId();
    
    i++;
   }
 
   
 //  attrList.setDataSource(ds);
   ListGridRecord[] records = new ListGridRecord[smpls.getSamples().size()];
 
   i=0;
   for( AttributedObject sample : smpls.getSamples() )
   {
    ListGridRecord rec = new ListGridRecord();
 
    rec.setAttribute("__obj", sample);
    rec.setAttribute("__id", "<span class='idCell'>"+sample.getFullName()+"</span>");
    
    for( AttributedObject at : sample.getAttributes() )
    {
     if( at.getFullName().equals(prideKey) )
      rec.setAttribute(at.getFullName(), "<a target='_blank' href='http://www.ebi.ac.uk/pride/directLink.do?experimentAccessionNumber="+at.getStringValue()+"'>"+at.getStringValue()+"</a>");
     else if( at.getAttributes() != null )
      rec.setAttribute(at.getFullName(), "<span class='qualifiedCell'>"+at.getStringValue()+"</span>");
     else
      rec.setAttribute(at.getFullName(), at.getStringValue());
    }
    records[i++]=rec;
   }
 
 //  ListGridRecord rec = new ListGridRecord();
 //  rec.setAttribute("__id", "XYN");
 //  ds.addData(rec);
 
   if( smpls.getTotalRecords() > ResultPane.MAX_SAMPLES_PER_PAGE )
   {
    pager.setContents(pg, smpls.getTotalRecords(),  ResultPane.MAX_SAMPLES_PER_PAGE, null, null );
    pager.setVisible(true);
   }
   else
    pager.setVisible(false);
 
   
   attrList.setFields(lfl);
   attrList.setData(records);
   
   attrList.setHoverCustomizer(new HoverCustomizer()
   {
    @Override
    public String hoverHTML(Object value, ListGridRecord record, int rowNum, int colNum)
    {
     if( colNum == 0)
      return null;
   
 //    return value.toString();
     return record.getAttributeAsString(lfl[colNum-1].getName());
    }
   });
   
   attrList.addCellClickHandler( new CellClickHandler()
   {
    @Override
    public void onCellClick(CellClickEvent event)
    {
     String attrId = header.get(event.getColNum()-1).getId();
     
     AttributedObject sample = (AttributedObject) event.getRecord().getAttributeAsObject("__obj");
     
     AttributedObject clickedObj = null;
     
     for( AttributedObject at : sample.getAttributes() )
     {
      if(attrId.equals(at.getFullName()))
      {
       clickedObj = at;
       break;
      }
     }
     
 //    System.out.println("Clicked: "+attrId+" -> "+clickedObj.getStringValue());
     
     if( event.getColNum() != lfl.length  )
     {
      if( clickedObj == null || clickedObj.getAttributes() == null || clickedObj.getAttributes().size() == 0 )
       return;
 
      new ObjectViewer(clickedObj).show();
      return;
     }
     
     new SampleViewer(header, event.getRecord()).show();
    }
   });
 
   Canvas[] membs = lay.getMembers();
   
   if( membs[membs.length-1] instanceof ListGrid )
    lay.removeMember(membs[membs.length-1]);
 
   
   lay.addMember(attrList);
 //  attrList.fetchData();
   
  }
 
 }
