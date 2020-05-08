 package uk.ac.ebi.biosd.xs.export;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import uk.ac.ebi.biosd.xs.log.LoggerFactory;
 import uk.ac.ebi.biosd.xs.util.Counter;
 import uk.ac.ebi.fg.biosd.model.access_control.User;
 import uk.ac.ebi.fg.biosd.model.expgraph.BioSample;
 import uk.ac.ebi.fg.biosd.model.expgraph.properties.SampleCommentValue;
 import uk.ac.ebi.fg.biosd.model.organizational.BioSampleGroup;
 import uk.ac.ebi.fg.biosd.model.organizational.MSI;
 import uk.ac.ebi.fg.biosd.model.xref.DatabaseRefSource;
 import uk.ac.ebi.fg.core_model.expgraph.Product;
 import uk.ac.ebi.fg.core_model.expgraph.properties.BioCharacteristicValue;
 import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyType;
 import uk.ac.ebi.fg.core_model.expgraph.properties.ExperimentalPropertyValue;
 import uk.ac.ebi.fg.core_model.organizational.Contact;
 import uk.ac.ebi.fg.core_model.organizational.ContactRole;
 import uk.ac.ebi.fg.core_model.organizational.Organization;
 import uk.ac.ebi.fg.core_model.organizational.Publication;
 import uk.ac.ebi.fg.core_model.terms.OntologyEntry;
 import uk.ac.ebi.fg.core_model.xref.ReferenceSource;
 
 public class AGE1XMLFormatter extends AbstractXMLFormatter
 {
 
  
  private static String nameSpace = "http://www.ebi.ac.uk/biosamples/SampleGroupExportV1";
 
  protected boolean nsShown=false;
  
  public AGE1XMLFormatter(boolean showAttributes, boolean showAC, SamplesFormat smpfmt, boolean pubOnly)
  {
   super( showAttributes, showAC, smpfmt, pubOnly );
  }
  
  protected String getNameSpace()
  {
   return nameSpace;
  }
  
  protected void exportSimpleValuePefix( Appendable out ) throws IOException
  {
   out.append("<value>");
  }
 
  protected void exportSimpleValuePostfix( Appendable out ) throws IOException
  {
   out.append("</value>\n");
  }
  
  protected void exportSimpleValueStringPefix( Appendable out ) throws IOException
  {
  }
 
  protected void exportSimpleValueStringPostfix( Appendable out ) throws IOException
  {
  }
 
  protected void exportObjectValuePrefix( Appendable out ) throws IOException
  {
   out.append("<value>");
  }
 
  protected void exportObjectValuePostfix( Appendable out ) throws IOException
  {
   out.append("</value>\n");
  }
  
  @Override
  public boolean exportSample(BioSample smp, Appendable out, boolean showNS) throws IOException
  {
   return exportSample(smp, out, showNS, isShowAttributes(), true, null, isShowAC());
  }
 
  
  
  @Override
  public boolean exportGroup(BioSampleGroup ao, Appendable out, boolean showNS) throws IOException
  {
   return exportGroup(ao, out, showNS, getSamplesFormat(), isShowAttributes(), isShowAC() );
  }
  
  protected interface ACObj
  {
   Set<User> getUsers();
   boolean isPublic();
  }
  
  protected void exportAC(ACObj ao, Appendable out) throws IOException
  {
   if(ao.isPublic())
    return;
   
   out.append("public=\"false\" access=\"");
 
   boolean first = true;
 
   for(User u : ao.getUsers())
   {
    if(!first)
     out.append(',');
    else
     first = false;
 
    out.append(u.getName());
   }
 
   out.append("\" ");
  }
 
  protected void exportSamples(BioSampleGroup ao, Appendable mainout, SamplesFormat smpSts, Set<String> attrset) throws IOException
  {
   if( smpSts != SamplesFormat.NONE && ao.getSamples() != null )
   {
    assert LoggerFactory.getLogger().entry("Start procesing sample block", "sblock");
 
    Collection<BioSample> smpls = ao.getSamples();
    
    assert LoggerFactory.getLogger().checkpoint("Got samples: "+smpls.size(), "sblock");
 
    for(BioSample smp : smpls)
    {
     if( ! isPublicOnly() || isSamplePublic(smp) )
      exportSample(smp, mainout, false, smpSts == SamplesFormat.EMBED, false, attrset, isShowAC());
    }
   
    assert LoggerFactory.getLogger().exit("End procesing sample block", "sblock");
   }
  }
  
  protected boolean exportGroup(final BioSampleGroup ao, Appendable mainout, boolean showNS, SamplesFormat smpSts, boolean showAttributes, boolean showAC) throws IOException
  {
   if( isPublicOnly() && ! isGroupPublic(ao) )
    return false;
 
   
   Set<String> attrset = null;
   
   if( showAttributes )
    attrset = new HashSet<>();
   
 
   if( showNS && ! nsShown )
   {
    mainout.append("<?xml version=\"1.1\" encoding=\"UTF-8\"?>\n");
    mainout.append("<SampleGroup xmlns=\""+getNameSpace()+"\" ");
   }
   else
    mainout.append("<SampleGroup ");
   
   if( showAC )
    exportAC( new ACObj()
    {
     @Override
     public boolean isPublic()
     {
      return isGroupPublic(ao);
     }
     
     @Override
     public Set<User> getUsers()
     {
      return ao.getUsers();
     }
    }, mainout);
 
   mainout.append("id=\"");
   xmlEscaped(ao.getAcc(), mainout);
   mainout.append("\">\n");
 
 
   MSI msi = null;
   
   if( ao.getMSIs() != null )
   {
    Iterator<MSI> it = ao.getMSIs().iterator();
    
    if( it.hasNext() )
     msi = it.next();
   }
 
 
   if( msi != null )
   {
    if( msi.getSubmissionDate() != null )
    {
     mainout.append("<attribute class=\"Submission Date\" classDefined=\"true\" dataType=\"DATETIME\">\n");
     exportSimpleValuePefix(mainout);
     exportSimpleValueStringPefix(mainout);
     mainout.append( dateTimeFmt.format(msi.getSubmissionDate() ) );
     exportSimpleValueStringPostfix(mainout);
     exportSimpleValuePostfix(mainout);
     mainout.append("</attribute>\n");
    }
    
    if( msi.getReleaseDate() != null )
    {
     mainout.append("<attribute class=\"Submission Release Date\" classDefined=\"true\" dataType=\"DATETIME\">\n");
     exportSimpleValuePefix(mainout);
     exportSimpleValueStringPefix(mainout);
     mainout.append( dateTimeFmt.format(msi.getReleaseDate() ) );
     exportSimpleValueStringPostfix(mainout);
     exportSimpleValuePostfix(mainout);
     mainout.append("</attribute>\n");
    }
 
    if( msi.getUpdateDate() != null )
    {
     mainout.append("<attribute class=\"Submission Update Date\" classDefined=\"true\" dataType=\"DATETIME\">\n");
     exportSimpleValuePefix(mainout);
     exportSimpleValueStringPefix(mainout);
     mainout.append( dateTimeFmt.format(msi.getUpdateDate() ) );
     exportSimpleValueStringPostfix(mainout);
     exportSimpleValuePostfix(mainout);
     mainout.append("</attribute>\n");
    }
    
    if( msi.getAcc() != null )
    {
     mainout.append("<attribute class=\"Submission Identifier\" classDefined=\"true\" dataType=\"STRING\">\n");
     exportSimpleValuePefix(mainout);
     exportSimpleValueStringPefix(mainout);
     xmlEscaped(msi.getAcc(), mainout);
     exportSimpleValueStringPostfix(mainout);
     exportSimpleValuePostfix(mainout);
     mainout.append("</attribute>\n");
    }
 
    
    if( msi.getTitle() != null )
    {
     mainout.append("<attribute class=\"Submission Title\" classDefined=\"true\" dataType=\"STRING\">\n");
     exportSimpleValuePefix(mainout);
     exportSimpleValueStringPefix(mainout);
     xmlEscaped(msi.getTitle(), mainout);
     exportSimpleValueStringPostfix(mainout);
     exportSimpleValuePostfix(mainout);
     mainout.append("</attribute>\n");
    }
    
    if( msi.getDescription() != null )
    {
     mainout.append("<attribute class=\"Submission Description\" classDefined=\"true\" dataType=\"STRING\">\n");
     exportSimpleValuePefix(mainout);
     exportSimpleValueStringPefix(mainout);
     xmlEscaped(msi.getDescription(), mainout);
     exportSimpleValueStringPostfix(mainout);
     exportSimpleValuePostfix(mainout);
     mainout.append("</attribute>\n");
    }
    
    if( msi.getVersion() != null )
    {
     mainout.append("<attribute class=\"Submission Version\" classDefined=\"true\" dataType=\"STRING\">\n");
     exportSimpleValuePefix(mainout);
     exportSimpleValueStringPefix(mainout);
     xmlEscaped(msi.getVersion(), mainout);
     exportSimpleValueStringPostfix(mainout);
     exportSimpleValuePostfix(mainout);
     mainout.append("</attribute>\n");
    }
 
    
    if( msi.getReferenceSources() != null && msi.getReferenceSources().size() > 0 )
    {
     mainout.append("<attribute class=\"Term Sources\" classDefined=\"true\" dataType=\"OBJECT\">\n");
 
     for( ReferenceSource c : msi.getReferenceSources() )
      exportReferenceSources(c, mainout);
     
     mainout.append("</attribute>\n");
    }
    
    if( msi.getOrganizations() != null && msi.getOrganizations().size() > 0 )
    {
     mainout.append("<attribute class=\"Organizations\" classDefined=\"true\" dataType=\"OBJECT\">\n");
 
     for( Organization c : msi.getOrganizations() )
      exportOrganization(c, mainout);    
  
     mainout.append("</attribute>\n");
    }
 
    if( msi.getContacts() != null )
    {
     mainout.append("<attribute class=\"Persons\" classDefined=\"true\" dataType=\"OBJECT\">\n");
 
     for( Contact c : msi.getContacts() )
      exportPerson(c, mainout); 
 
     mainout.append("</attribute>\n");
    }
    
    if( msi.getDatabases() != null )
    {
     mainout.append("<attribute class=\"Databases\" classDefined=\"true\" dataType=\"OBJECT\">\n");
 
     for( DatabaseRefSource c : msi.getDatabases() )
      exportDatabase(c, mainout);
 
     mainout.append("</attribute>\n");
    }
    
    if( msi.getPublications() != null )
    {
     mainout.append("<attribute class=\"Publications\" classDefined=\"true\" dataType=\"OBJECT\">\n");
 
     for( Publication c : msi.getPublications() )
      exportPublication(c, mainout);
 
     mainout.append("</attribute>\n");
    }
 
   }
 
   
   if( ao.getPropertyValues() != null )
   {
    exportPropertyValues(ao.getPropertyValues(), mainout, null);
   }
   
 
   exportSamples( ao, mainout, smpSts, attrset);
   
   if( showAttributes )
   {
    mainout.append("<SampleAttributes>\n");
 
    for(String attNm : attrset)
    {
     mainout.append("<attribute class=\"");
     xmlEscaped(attNm, mainout);
     mainout.append("\" classDefined=\"true\" dataType=\"STRING\"/>\n");
    }
 
    mainout.append("</SampleAttributes>\n");
   }
   
   mainout.append("</SampleGroup>\n");
  
   return true;
  }
 
  @Override
  public void exportHeader( long since, Appendable out, boolean showNS ) throws IOException
  {
   Date startTime = new java.util.Date();
 
   
   out.append("<?xml version=\"1.1\" encoding=\"UTF-8\"?>\n");
   out.append("<Biosamples");
   
   if( showNS )
   {
    out.append(" xmlns=\"");
    xmlEscaped(getNameSpace(), out);
    out.append("\"");
    
    nsShown = true;
   }
   
   if( since > 0 )
    out.append(" since=\"").append( String.valueOf(since) ).append("\"");
 
   out.append(" timestamp=\"").append( String.valueOf(startTime.getTime()) ).append("\"");
   
   out.append(" >\n");
  }
 
 
  @Override
  public void exportFooter(Appendable out) throws IOException
  {
   out.append("</Biosamples>\n");
  }
  
  @Override
  public void exportSources(Map<String, Counter> srcMap, Appendable out) throws IOException
  {
   out.append("<Datasources>\n");
   
   for( Map.Entry<String, Counter> me : srcMap.entrySet() )
   {
    out.append(" <Datasource name=\"");
    xmlEscaped(me.getKey(), out);
    out.append("\" count=\"");
    out.append(String.valueOf(me.getValue().intValue()));
    out.append("\" />\n");
   }
   
   out.append("</Datasources>\n");
  }
  
  private void exportReferenceSources( ReferenceSource rs, Appendable out ) throws IOException
  {
   exportObjectValuePrefix( out );
   out.append("\n<object id=\"");
   xmlEscaped(rs.getAcc(), out);
   out.append("\" class=\"TermSource\" classDefined=\"true\">\n");
   
   String s = null;
   
   s = rs.getName();
   if( s != null && s.length() > 0 )
    exportAttribute("Term Source Name", s, out);
   
   s = rs.getUrl();
   if( s != null && s.length() > 0 )
    exportAttribute("Term Source URI", s, out);
   
   s = rs.getVersion();
   if( s != null && s.length() > 0 )
    exportAttribute("Term Source Version", s, out);
 
   out.append("</object>\n");
   exportObjectValuePostfix( out );
  }
  
  private void exportOrganization( Organization org, Appendable out ) throws IOException
  {
   exportObjectValuePrefix( out );
   out.append("\n<object id=\"");
   out.append( String.valueOf(org.getId()) );
   out.append("\" class=\"Organization\" classDefined=\"true\">\n");
   
   String s = null;
   
   s = org.getName();
   if( s != null && s.length() > 0 )
    exportAttribute("Organization Name", s, out);
 
   s = org.getAddress();
   if( s != null && s.length() > 0 )
    exportAttribute("Organization Address", s, out);
   
   s = org.getUrl();
   if( s != null && s.length() > 0 )
    exportAttribute("Organization URI", s, out);
   
   s = org.getEmail();
   if( s != null && s.length() > 0 )
    exportAttribute("Organization Email", s, out);
 
   if( org.getOrganizationRoles() != null && org.getOrganizationRoles().size() > 0  )
    exportRoles("Organization Role", org.getOrganizationRoles(), out);
    
   out.append("</object>\n");
   exportObjectValuePostfix( out );
  }
  
  private void exportPerson( Contact cnt, Appendable out ) throws IOException
  {
   exportObjectValuePrefix( out );
   out.append("\n<object id=\"");
   out.append( String.valueOf(cnt.getId()) );
  out.append("\" class=\"Organization\" classDefined=\"true\">\n");
   
   String s = null;
   
   s = cnt.getFirstName();
   if( s != null && s.length() > 0 )
    exportAttribute("Person First Name", s, out);
 
   s = cnt.getLastName();
   if( s != null && s.length() > 0 )
    exportAttribute("Person Last Name", s, out);
 
   s = cnt.getMidInitials();
   if( s != null && s.length() > 0 )
    exportAttribute("Person Initials", s, out);
  
   s = cnt.getEmail();
   if( s != null && s.length() > 0 )
    exportAttribute("Person Email", s, out);
 
   if( cnt.getContactRoles() != null && cnt.getContactRoles().size() > 0  )
    exportRoles("Person Role", cnt.getContactRoles(), out);
   
   out.append("</object>\n");
   exportObjectValuePostfix( out );
  }
  
  private void exportPublication( Publication pub, Appendable out ) throws IOException
  {
   exportObjectValuePrefix( out );
   out.append("\n<object id=\"");
   out.append( String.valueOf(pub.getId()) );
   out.append("\" class=\"Publication\" classDefined=\"true\">\n");
   
   String s = null;
   
   s = pub.getDOI();
   if( s != null && s.length() > 0 )
    exportAttribute("Publication DOI", s, out);
   
   s = pub.getPubmedId();
   if( s != null && s.length() > 0 )
    exportAttribute("Publication PubMed ID", s, out);
 
   out.append("</object>\n");
   exportObjectValuePostfix( out );
  }
  
  private void exportDatabase( DatabaseRefSource cnt, Appendable out ) throws IOException
  {
   exportObjectValuePrefix( out );
   out.append("\n<object id=\"");
   out.append( String.valueOf(cnt.getId()) );
   out.append("\" class=\"Database\" classDefined=\"true\">\n");
   
   String s = null;
   
   s = cnt.getName();
   if( s != null && s.length() > 0 )
    exportAttribute("Database Name", s, out);
 
   s = cnt.getUrl();
   if( s != null && s.length() > 0 )
    exportAttribute("Database URI", s, out);
 
   s = cnt.getAcc();
   if( s != null && s.length() > 0 )
    exportAttribute("Database ID", s, out);
  
   out.append("</object>\n");
   exportObjectValuePostfix( out );
  }
  
  private void exportAttribute( String cls, String val, Appendable out ) throws IOException
  {
   out.append("<attribute class=\"");
   xmlEscaped(cls, out);
   out.append("\" classDefined=\"true\" dataType=\"STRING\">\n");
   exportSimpleValuePefix(out);
   exportSimpleValueStringPefix(out);
   xmlEscaped(val,out);
   exportSimpleValueStringPostfix(out);
   exportSimpleValuePostfix(out);
   out.append("</attribute>\n");
  }
  
  private void exportRoles( String cls, Collection<ContactRole> roles, Appendable out ) throws IOException
  {
   out.append("<attribute class=\"");
   xmlEscaped(cls, out);
   out.append("\" classDefined=\"true\" dataType=\"STRING\">\n");
   
   for( ContactRole cr : roles )
   {
    exportSimpleValuePefix(out);
    exportSimpleValueStringPefix(out);
    xmlEscaped(cr.getName(), out);
    exportSimpleValueStringPostfix(out);
    exportSimpleValuePostfix(out);
   }
 
   out.append("</attribute>\n");
  }
  
  protected boolean exportSample(final BioSample smp, Appendable mainout, boolean showNS, boolean showAnnt, boolean showGrpId, Set<String> attrset, boolean showAC) throws IOException
  {
   if( isPublicOnly() && ! isSamplePublic(smp) )
    return false;
 
    
   if( showNS && ! nsShown )
   {
    mainout.append("<?xml version=\"1.1\" encoding=\"UTF-8\"?>\n");
 
    mainout.append( "<Sample xmlns=\"");
    xmlEscaped(getNameSpace(), mainout);
    mainout.append( "\" ");
   }
   else
    mainout.append("<Sample ");
   
   if( showAC )
    exportAC(new ACObj()
    {
     @Override
     public boolean isPublic()
     {
      return smp.isPublic();
     }
     
     @Override
     public Set<User> getUsers()
     {
      return smp.getUsers();
     }
    }, mainout);
 
   mainout.append("id=\"");
   xmlEscaped(smp.getAcc(), mainout);
  
 
   mainout.append("\">\n");
   
   if(smp.getPropertyValues() != null && (showAnnt || attrset != null))
   {
    exportPropertyValues(smp.getPropertyValues(), mainout, attrset);
   }
   
   Collection<DatabaseRefSource> dbs = smp.getDatabases();
   
   if( dbs != null && dbs.size() > 0 )
   {
    mainout.append("<attribute class=\"Databases\" classDefined=\"true\" dataType=\"OBJECT\">\n");
 
    for( DatabaseRefSource c : dbs )
     exportDatabase(c, mainout);
 
    mainout.append("</attribute>\n");
   }
 
   if(showAnnt && smp.getAllDerivedFrom() != null)
   {
    for(Product< ? > p : smp.getAllDerivedFrom())
    {
     mainout.append("<relation class=\"derivedFrom\" targetId=\"");
     xmlEscaped(p.getAcc(), mainout);
     mainout.append("\" targetClass=\"Sample\" />\n");
    }
   }
   
   if(showGrpId)
   {
    mainout.append("<GroupIds>\n");
    
    for(BioSampleGroup sg : smp.getGroups())
    {
     mainout.append("<Id>");
     xmlEscaped(sg.getAcc(), mainout);
     mainout.append("</Id>\n");
    }
    
    mainout.append("</GroupIds>\n");
    
   }
   
   mainout.append("</Sample>\n");
  
   return true;
  }
 
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void exportPropertyValues( Collection<ExperimentalPropertyValue> orgVals,  Appendable out, Set<String> attrset ) throws IOException
  {
   ArrayList<ExperimentalPropertyValue> vals = new ArrayList<>( orgVals );
   ArrayList<ExperimentalPropertyValue<? extends ExperimentalPropertyType>> procV = new ArrayList<>( vals.size() );
   
   for( int i=0; i < vals.size(); i++ )
   {
    procV.clear();
    
    ExperimentalPropertyValue<ExperimentalPropertyType> v = vals.get(i);
    
    if( v == null )
     continue;
    
    procV.add( v );
    
    for( int j = i+1; j < vals.size(); j++ )
    {
     ExperimentalPropertyValue<ExperimentalPropertyType> nv = vals.get(j);
     
     if( nv == null )
      continue;
     
     if( nv.getType().getTermText().equals( v.getType().getTermText() ) )
     {
      procV.add( nv );
      vals.set(j, null);
     }
    }
    
    exportPropertyValue(procV, out);
    
    if(attrset != null)
     attrset.add(v.getType().getTermText());
 
   }
   
  }
 
  private void exportPropertyValue( List<ExperimentalPropertyValue<? extends ExperimentalPropertyType>> vals,  Appendable out) throws IOException
  {
   ExperimentalPropertyValue<? extends ExperimentalPropertyType> val0 = vals.get(0);
   
   boolean isChar = ( val0 instanceof BioCharacteristicValue );
   boolean isComm = ( val0 instanceof SampleCommentValue );
   
   out.append("<attribute classDefined=\"false\" dataType=\"STRING\" ");
   
   if( isChar )
    out.append("characteristic=\"true\" ");
   else if( isComm )
    out.append("comment=\"true\" ");
 
   out.append("class=\"");
   xmlEscaped(val0.getType().getTermText(),out);
   out.append("\">\n");
   
   for(  ExperimentalPropertyValue<? extends ExperimentalPropertyType> pv : vals  )
   {
    exportSimpleValuePefix(out);
 
    exportSimpleValueStringPefix(out);
    xmlEscaped(pv.getTermText(), out);
    exportSimpleValueStringPostfix(out);
 
    if(pv.getUnit() != null)
    {
     out.append("<attribute classDefined=\"true\" dataType=\"OBJECT\" class=\"Unit\">\n");
     exportSimpleValuePefix(out);
     exportSimpleValueStringPefix(out);
     xmlEscaped(pv.getUnit().getTermText(), out);
     exportSimpleValueStringPostfix(out);
     exportSimpleValuePostfix(out);
 
     Collection<OntologyEntry> unitont = pv.getUnit().getOntologyTerms();
 
     if(unitont != null)
     {
      for(OntologyEntry oe : unitont)
       exportOntologyEntry(oe, out);
     }
 
     out.append("</attribute>\n");
    }
 
    if(pv.getOntologyTerms() != null)
    {
     for(OntologyEntry oe : pv.getOntologyTerms())
      exportOntologyEntry(oe, out);
    }
 
    exportSimpleValuePostfix(out);
   }
   out.append("</attribute>\n");
  }
  
 
  
  private void exportOntologyEntry( OntologyEntry val,  Appendable out) throws IOException
  {
   ReferenceSource src = val.getSource();
 
   out.append("<attribute class=\"Term Source REF\" classDefined=\"true\" dataType=\"OBJECT\">\n");
   exportObjectValuePrefix(out);
   out.append("\n<object id=\""+src.getName()+"\" class=\"Term Source\" classDefined=\"true\">\n");
   
   
   if( src != null )
   {
    if( src.getName() != null )
    {
     out.append("<attribute class=\"Term Source Name\" classDefined=\"true\" dataType=\"STRING\">\n");
 
     exportSimpleValuePefix(out);
     exportSimpleValueStringPefix(out);
     xmlEscaped(src.getName(),out);
     exportSimpleValueStringPostfix(out);
     exportSimpleValuePostfix(out);
 
     out.append("</attribute>\n");
    }
    
   
    if( src.getUrl() != null )
    {
     out.append("<attribute class=\"Term Source URI\" classDefined=\"true\" dataType=\"STRING\">\n");
 
     exportSimpleValuePefix(out);
     exportSimpleValueStringPefix(out);
     xmlEscaped(src.getUrl(),out);
     exportSimpleValueStringPostfix(out);
     exportSimpleValuePostfix(out);
 
     out.append("</attribute>\n");
    }
 
    if( src.getVersion() != null )
    {
     out.append("<attribute class=\"Term Source Version\" classDefined=\"true\" dataType=\"STRING\">\n");
 
     exportSimpleValuePefix(out);
     exportSimpleValueStringPefix(out);
     xmlEscaped(src.getVersion(),out);
     exportSimpleValueStringPostfix(out);
     exportSimpleValuePostfix(out);
 
     out.append("</attribute>\n");
    }
 
   }
   
   if( val.getAcc() != null )
   {
    out.append("<attribute class=\"Term Source ID\" classDefined=\"true\" dataType=\"STRING\">\n");
 
    exportSimpleValuePefix(out);
    exportSimpleValueStringPefix(out);
    xmlEscaped(val.getAcc(),out);
    exportSimpleValueStringPostfix(out);
    exportSimpleValuePostfix(out);
 
    out.append("</attribute>\n");
   }
   
   out.append("</object>\n");
   exportObjectValuePostfix(out);
   out.append("</attribute>\n");
 
  }
  
  @Override
  public void shutdown()
  {}
 
  @Override
  public boolean isSamplesExport()
  {
   return false;
  }
 
  @Override
  public void exportGroupHeader(Appendable out, boolean showNS) throws IOException
  {
   return;
  }
 
  @Override
  public void exportSampleHeader(Appendable out, boolean showNS) throws IOException
  {
   return;
  }
 
  @Override
  public void exportGroupFooter(Appendable out) throws IOException
  {
   return;
  }
 
  @Override
  public void exportSampleFooter(Appendable out) throws IOException
  {
   return;
  }
  
 }
