 package uk.ac.ebi.biosd.xs.export;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TimeZone;
 
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
 import uk.ac.ebi.fg.core_model.toplevel.Annotation;
 import uk.ac.ebi.fg.core_model.toplevel.DefaultAccessibleAnnotatable;
 import uk.ac.ebi.fg.core_model.xref.ReferenceSource;
 
 
 
 public class STXMLFormatter extends AGE1XMLFormatter
 {
  
 // public static final String updateDate = "Submission Update Date";
 // public static final String releaseDate = "Submission Release Date";
 // public static final String refLayer = "Reference Layer";
  
  
  public STXMLFormatter( boolean showAttributes, boolean showAC, SamplesFormat smpfmt, boolean pubOnly)
  {
   super( showAttributes, showAC, smpfmt, pubOnly);
  }
 
 
 
  public static final String nameSpace = "http://www.ebi.ac.uk/biosamples/BioSDExportV1";
 
  private static DateToXsdDatetimeFormatter dateTimeFmt = new DateToXsdDatetimeFormatter( TimeZone.getTimeZone("GMT") );
  
  
  @Override
  public void exportHeader(long since,  Appendable out, boolean showNS) throws IOException
  {
   out.append("<BioSamples");
   
   if( showNS )
   {
    out.append(" xmlns=\"");
    xmlEscaped(nameSpace, out);
    out.append("\"");
    
    nsShown = true;
   }
   
   if( since > 0 )
    out.append(" since=\"").append( String.valueOf(since) ).append("\"");
 
   out.append(" timestamp=\"").append( String.valueOf(new Date().getTime()) ).append("\"");
  
   out.append(" >\n");
  }
 
  @Override
  public void exportFooter(Appendable out) throws IOException
  {
   out.append("</BioSamples>\n");
  }
  
  @Override
  public boolean exportSample(BioSample smp,  Appendable out, boolean showNS) throws IOException
  {
   return exportSample(smp, out, showNS, true, null, null, isShowAC() );
  }
  
  @Override
  public boolean exportGroup( BioSampleGroup ao, Appendable out, boolean showNS ) throws IOException
  {
   return exportGroup(ao, out, showNS, getSamplesFormat(), isShowAttributes(), isShowAC() );
  }
 
  @Override
  protected boolean exportGroup( final BioSampleGroup ao, Appendable out, boolean showNS, SamplesFormat smpSts, boolean showAttributes, boolean showAC ) throws IOException
  {
   if( isPublicOnly() && ! isGroupPublic(ao) )
    return false;
 
   Set<String> attrset = null;
   
   if( showAttributes )
    attrset = new HashSet<>();
   
   out.append("<BioSampleGroup ");
   
 
   if( showNS && ! nsShown )
    out.append("xmlns=\""+getNameSpace()+"\" ");
 
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
    }, out);
   
   out.append("id=\"");
   xmlEscaped(ao.getAcc(), out);
   out.append("\">\n");
 
 
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
     out.append("<SubmissionDate>");
     out.append( dateTimeFmt.format(msi.getSubmissionDate() ) );
     out.append("</SubmissionDate>\n");
    }
    
    if( msi.getReleaseDate() != null )
    {
     out.append("<ReleaseDate>");
     out.append( dateTimeFmt.format(msi.getReleaseDate() ) );
     out.append("</ReleaseDate>\n");
    }
 
    if( msi.getUpdateDate() != null )
    {
     out.append("<UpdateDate>");
     out.append( dateTimeFmt.format(msi.getUpdateDate() ) );
     out.append("</UpdateDate>\n");
    }
    
    
    if( msi.getAcc() != null )
    {
     out.append("<SubmissionIdentifier>");
     xmlEscaped( msi.getAcc(), out);
     out.append("</SubmissionIdentifier>\n");
    }
 
    
    if( msi.getTitle() != null )
    {
     out.append("<SubmissionTitle>");
     xmlEscaped( msi.getTitle() , out);
     out.append("</SubmissionTitle>\n");
    }
    
    if( msi.getDescription() != null )
    {
     out.append("<SubmissionDescription>");
     xmlEscaped( msi.getDescription() , out);
     out.append("</SubmissionDescription>\n");
    }
    
    if( msi.getVersion() != null )
    {
     out.append("<SubmissionVersion>");
     xmlEscaped( msi.getVersion() , out);
     out.append("</SubmissionVersion>\n");
    }
 
    if( msi.getReferenceSources() != null )
    {
     for( ReferenceSource c : msi.getReferenceSources() )
      exportReferenceSources(c, out);
    }
    
    if( msi.getOrganizations() != null )
    {
     for( Organization c : msi.getOrganizations() )
      exportOrganization(c, out);
    }
 
    if( msi.getContacts() != null )
    {
     for( Contact c : msi.getContacts() )
      exportPerson(c, out);
    }
    
    if( msi.getDatabases() != null )
    {
     for( DatabaseRefSource c : msi.getDatabases() )
      exportDatabase(c, out);
    }
    
    if( msi.getPublications() != null )
    {
     for( Publication c : msi.getPublications() )
      exportPublication(c, out);
    }
 
   }
 
   
   if( ao.getPropertyValues() != null )
   {
    for( ExperimentalPropertyValue<ExperimentalPropertyType> pval : ao.getPropertyValues() )
     exportPropertyValue(pval,out);
   }
   
   exportAnnotations(ao, out);
 
   
   if( smpSts != SamplesFormat.NONE && ao.getSamples() != null )
   {
     for( BioSample smp : ao.getSamples() )
     {
      exportSample(smp, out, false, smpSts == SamplesFormat.EMBED, ao.getAcc(), attrset, isShowAC());
     }
    
   }
   
   if( showAttributes )
   {
    out.append("<SampleAttributes>\n");
 
    for(String attNm : attrset)
    {
     out.append("<attribute>");
     xmlEscaped(attNm, out);
     out.append("</attribute>\n");
    }
 
    out.append("</SampleAttributes>\n");
   }
   
   
   out.append("</BioSampleGroup>\n");
   
   return true;
  }
  
  private static void exportOntologyEntry( OntologyEntry val,  Appendable out) throws IOException
  {
   out.append("<TermSourceREF>\n");
   
   
   ReferenceSource src = val.getSource();
   if( src != null )
   {
    if( src.getName() != null )
    {
     out.append("<Name>");
     xmlEscaped(src.getName(), out);
     out.append("</Name>\n");
    }
    
   
    if( src.getUrl() != null )
    {
     out.append("<URI>");
     xmlEscaped(src.getUrl(), out);
     out.append("</URI>\n");
    }
 
    if( src.getVersion() != null )
    {
     out.append("<Version>");
     xmlEscaped(src.getVersion(), out);
     out.append("</Version>\n");
    }
 
   }
   
   if( val.getAcc() != null )
   {
    out.append("<TermSourceID>");
    xmlEscaped(val.getAcc(), out);
    out.append("</TermSourceID>\n");
   }
   
   out.append("</TermSourceREF>\n");
 
  }
  
 
 
  private boolean exportSample(final BioSample smp,  Appendable out, boolean showNS, boolean showAnnt, String grpId, Set<String> attrset, boolean showAC) throws IOException
  {
   if( isPublicOnly() && ! isSamplePublic(smp) )
    return false;
 
   
   out.append("<BioSample ");
   
   if( showNS && ! nsShown )
   {
    out.append( "xmlns=\"");
    xmlEscaped(getNameSpace(), out);
    out.append( "\" ");
   }
   
   if( showAC )
    exportAC(new ACObj()
    {
     @Override
     public boolean isPublic()
     {
      return isSamplePublic(smp);
     }
     
     @Override
     public Set<User> getUsers()
     {
      return smp.getUsers();
     }
    }, out);
 
   out.append("id=\"");
   xmlEscaped(smp.getAcc(), out);
  
   if( grpId != null )
   {
    out.append("\" groupId=\"");
    xmlEscaped(grpId, out);
   }
   
   if( ! showAnnt )
   {
    out.append("\"/>\n");
    return true;
   }
  
   out.append("\">\n");
  
   exportAnnotations(smp, out);
   
   if( smp.getPropertyValues() != null )
   {
    for( ExperimentalPropertyValue<ExperimentalPropertyType> pval : smp.getPropertyValues() )
    {
     exportPropertyValue(pval,out);
     
     if( attrset != null )
      attrset.add(pval.getType().getTermText());
    }
   }
   
  if( smp.getDatabases() != null )
  {
   for( DatabaseRefSource c : smp.getDatabases() )
    exportDatabase(c, out);
  }
  
   if( smp.getAllDerivedFrom() != null )
   {
    for( Product<?> p : smp.getAllDerivedFrom() )
    {
     out.append("<derivedFrom>");
     xmlEscaped(p.getAcc(), out);
     out.append("</derivedFrom>\n");
    }
   }
   
   Set<BioSampleGroup> grps = smp.getGroups();
   
   if( grps != null )
   {
    out.append("\n");
    for( BioSampleGroup grp : grps )
    {
     out.append("<GroupRef>");
     xmlEscaped(grp.getAcc(), out);
     out.append("</GroupRef>\n");
    }
   }
   
   
   out.append("</BioSample>\n");
 
   
   return true;
  }
  
  private static void exportAnnotations( DefaultAccessibleAnnotatable obj, Appendable out ) throws IOException
  {
   if(obj.getAnnotations() == null)
    return;
   
   for(Annotation annt : obj.getAnnotations())
   {
    out.append("<Annotation type=\"");
    xmlEscaped(annt.getType().getName(), out);
    out.append("\">");
    xmlEscaped(annt.getText(), out);
    out.append("\n</Annotation>\n");
   }
  }
  
  private static void exportPropertyValue( ExperimentalPropertyValue<? extends ExperimentalPropertyType> val,  Appendable out) throws IOException
  {
   boolean isChar = ( val instanceof BioCharacteristicValue );
   boolean isComm = ( val instanceof SampleCommentValue );
   
   out.append("<Property ");
   
   if( isChar )
    out.append("characteristic=\"true\" ");
   else if( isComm )
    out.append("comment=\"true\" ");
 
   out.append("type=\"");
   xmlEscaped(val.getType().getTermText(),out);
   out.append("\">\n");
   out.append("<Value>");
   xmlEscaped(val.getTermText(),out);
   out.append("</Value>\n");
 
   if( val.getUnit() != null )
   {
    out.append("<Unit>");
    out.append("<Value>");
    xmlEscaped(val.getUnit().getTermText(),out);
    out.append("</Value>\n");  
    
    Collection<OntologyEntry> unitont = val.getUnit().getOntologyTerms();
    
    if( unitont != null )
    {
     for( OntologyEntry oe: unitont )
      exportOntologyEntry( oe, out );
    }
    
 
    out.append("</Unit>\n");
   }
   
   if( val.getOntologyTerms() != null )
   {
    for( OntologyEntry oe: val.getOntologyTerms() )
     exportOntologyEntry( oe, out );
   }
   
   out.append("</Property>\n");
  }
  
 
  
  private static void exportPerson( Contact cnt, Appendable out ) throws IOException
  {
   out.append("<Person>\n");
   
   String s = null;
   
   s = cnt.getLastName();
   if( s != null && s.length() > 0 )
   {
    out.append("<FirstName>");
    xmlEscaped(s, out);
    out.append("</FirstName>\n");
   }
   
   s = cnt.getLastName();
   if( s != null && s.length() > 0 )
   {
    out.append("<LastName>");
    xmlEscaped(s, out);
    out.append("</LastName>\n");
   }
   
   s = cnt.getMidInitials();
   if( s != null && s.length() > 0 )
   {
    out.append("<Initials>");
    xmlEscaped(s, out);
    out.append("</Initials>\n");
   }
  
   s = cnt.getEmail();
   if( s != null && s.length() > 0 )
   {
    out.append("<Email>");
    xmlEscaped(s, out);
    out.append("</Email>\n");
   }
   
   if( cnt.getContactRoles() != null )
   {
    for( ContactRole cr : cnt.getContactRoles() )
    {
     out.append("<Role>");
     xmlEscaped(cr.getName(), out);
     out.append("</Role>\n");
    }
   }
   
   out.append("</Person>\n");
 
  }
  
  private static void exportDatabase( DatabaseRefSource cnt, Appendable out ) throws IOException
  {
   out.append("<Database>\n");
   
   String s = null;
   
   s = cnt.getName();
   if( s != null && s.length() > 0 )
   {
    out.append("<Name>");
    xmlEscaped(s, out);
    out.append("</Name>\n");
   }
   
   s = cnt.getUrl();
   if( s != null && s.length() > 0 )
   {
    out.append("<URI>");
    xmlEscaped(s, out);
    out.append("</URI>\n");
   }
 
   s = cnt.getAcc();
   if( s != null && s.length() > 0 )
   {
    out.append("<ID>");
    xmlEscaped(s, out);
    out.append("</ID>\n");
   }
   
 
   out.append("</Database>\n");
 
  }
  
  private static void exportOrganization( Organization org, Appendable out ) throws IOException
  {
   out.append("<Organization>\n");
   
   String s = null;
   
   s = org.getName();
   if( s != null && s.length() > 0 )
   {
    out.append("<Name>");
    xmlEscaped(s, out);
    out.append("</Name>\n");
   }
 
   s = org.getAddress();
   if( s != null && s.length() > 0 )
   {
    out.append("<Address>");
    xmlEscaped(s, out);
    out.append("</Address>\n");
   }
   
   s = org.getUrl();
   if( s != null && s.length() > 0 )
   {
    out.append("<URI>");
    xmlEscaped(s, out);
    out.append("</URI>\n");
   }
   
   s = org.getEmail();
   if( s != null && s.length() > 0 )
   {
    out.append("<Email>");
    xmlEscaped(s, out);
    out.append("</Email>\n");
   }
 
   if( org.getOrganizationRoles() != null )
   {
    for( ContactRole cr : org.getOrganizationRoles() )
    {
     out.append("<Role>");
     xmlEscaped(cr.getName(), out);
     out.append("</Role>\n");
    }
   }
 
   out.append("</Organization>\n");
 
  }
 
  private static void exportPublication( Publication pub, Appendable out ) throws IOException
  {
   out.append("<Publication>\n");
   
   String s = null;
   
   s = pub.getDOI();
   if( s != null && s.length() > 0 )
   {
    out.append("<DOI>");
    xmlEscaped(s, out);
    out.append("</DOI>\n");
   }
   
   s = pub.getPubmedId();
   if( s != null && s.length() > 0 )
   {
    out.append("<PubMedID>");
    xmlEscaped(s, out);
    out.append("</PubMedID>\n");
   }
 
   out.append("</Publication>");
 
  }
 
  
  private static void exportReferenceSources( ReferenceSource rs, Appendable out ) throws IOException
  {
   out.append("<TermSource>\n");
   
   String s = null;
   
   s = rs.getName();
   if( s != null && s.length() > 0 )
   {
    out.append("<Name>");
    xmlEscaped(s, out);
    out.append("</Name>\n");
   }
   
   s = rs.getUrl();
   if( s != null && s.length() > 0 )
   {
    out.append("<URI>");
    xmlEscaped(s, out);
    out.append("</URI>\n");
   }
   
   s = rs.getVersion();
   if( s != null && s.length() > 0 )
   {
    out.append("<Version>");
    xmlEscaped(s, out);
    out.append("</Version>\n");
   }
 
   out.append("</TermSource>\n");
 
  }
 
 
 
  @Override
  public void exportSources(Map<String, Counter> srcMap, Appendable out) throws IOException
  {
  }
 
 }
