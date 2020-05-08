 package uk.ac.ebi.sampletab;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class ATWriter
 {
  public static final int MAX_INLINE_REPEATS=3;
  
  public static final char TAB = '\t';
  
  private static Pattern attrPattern = Pattern.compile("^\\s*(\\S+)\\s*\\[\\s*(.+)\\s*\\]\\s*");
  
  public static void writeAgeTab(Submission sub, OutputStream outStream) throws IOException
  {
   PrintStream out = new PrintStream(outStream, false, "UTF-8");
 
 //  out.print('|');
   
   out.print( Definitions.SUBMISSION );
   out.print( TAB );
   out.print( sub.getID() );
   
   for( Attribute attr : sub.getAnnotations() )
   {
    String atName = attr.getName();
    
    if( atName.startsWith(Definitions.SUBMISSION) )
     atName = atName.substring(Definitions.SUBMISSION.length()+1);
 
    out.println();
    out.print( atName );
    out.print( TAB );
    out.print( attr.getID() );
   }
 
   for( String atCl : sub.getAttachedClasses() )
   {
    List<WellDefinedObject> objs = sub.getAttachedObjects(atCl);
    
    if( objs==null || objs.size() == 0 )
     continue;
    
    out.println();
    out.print(atCl);
    
    for( WellDefinedObject obj : objs )
    {
     out.print( TAB );
     out.print( obj.getID() );
    }
    
   }
 
 
   for( String atCl : sub.getAttachedClasses() )
   {
    List<WellDefinedObject> objs = sub.getAttachedObjects(atCl);
    
    if( objs.size() == 0 )
     continue;
    
    out.println();
    out.println();
 //   out.print('|');
    out.print( atCl );
 
    for( WellDefinedObject obj : objs )
    {
     out.print( TAB );
     out.print( obj.getID() );
    }
 
  
    
    for( String fld : Definitions.object2Properties.get(atCl) )
    {
     boolean has = false;
     
     for( WellDefinedObject obj : objs )
     {
      if( obj.getAnnotation(fld) != null )
      {
       has = true;
       break;
      }
     }
     
     if( has )
     {
      out.println();
 
      if( fld.startsWith(atCl) )
       out.print(fld.substring(atCl.length()+1) );
      else
       out.print(fld);
 
      for( WellDefinedObject obj : objs )
      {
       out.print( TAB );
       out.print( obj.getAnnotation(fld).getID() );
      }
      
     }
    }
    
   }
   
   for( Group grp : sub.getGroups() )
   {
    out.println();
    out.println();
 //   out.print('|');
    out.print(Definitions.GROUP);
    out.print(TAB);
    out.print(grp.getID());
    
    for( Attribute attr : grp.getAnnotations() )
    {
     String attrName = attr.getName();
     
     if( attrName.startsWith(Definitions.SUBMISSION) )
      attrName = attrName.substring(Definitions.SUBMISSION.length()+1);
     
     out.println();
     out.print(attrName);
     
     if( attr.getValuesNumber() == 1 )
     {
      out.print(TAB);
      out.print(attr.getID());
     }
     else
     {
      for( Attribute valAt : attr.getValues() )
      {
       out.print(TAB);
       out.print(valAt.getID());
      }
      
     }
    }
    
    for( String atOCls : grp.getAttachedClasses() )
    {
     List<WellDefinedObject> objs = grp.getAttachedObjects( atOCls );
     
     out.println();
     out.print(atOCls);
 
     for( WellDefinedObject o : objs )
     {
      out.print(TAB);
      out.print(o.getID());
     }
     
    }
    
   }
   
   for( List<Sample> sBlock : sub.getSampleBlocks() )
   {
 //   List<Attribute> protoAttrMap = new LinkedList<Attribute>();
 //   List<Group> protoGrpMap = new LinkedList<Group>();
 //   List<Sample> protoDervMap = new LinkedList<Sample>();
    
    Sample proto = new Sample();
    proto.setID(Definitions.PROTOTYPEID);
    
    Sample s0 = sBlock.get(0);
    
    for( Attribute at : s0.getAnnotations() )
     proto.addAnnotation(at);
 
    if( s0.getGroups() != null )
    {
     for( Group g : s0.getGroups() )
      proto.addGroup(g);
    }
    
    if( s0.getDeriverFromSamples() != null )
    {
     for( Sample s : s0.getDeriverFromSamples() )
      proto.addDerivedFrom( s );
    }
 
    for( int i=1; i < sBlock.size(); i++ )
    {
     Sample s = sBlock.get(i);
     
     Iterator<Attribute> attIter = proto.getAnnotations().iterator();
     
     while( attIter.hasNext() )
     {
      Attribute at = attIter.next();
      Attribute chkat = s.getAnnotation( at.getName() );
      
      if( chkat == null || ! at.equals( chkat ) )
       attIter.remove();
     }
     
     if( s.getGroups() == null )
      proto.getGroups().clear();
     else
     {
      Iterator<? extends Group> grpIter = proto.getGroups().iterator();
      
      while( grpIter.hasNext() )
      {
       Group g = grpIter.next();
       
       boolean found = false;
       
       for( Group sg : s.getGroups() )
       {
        if( sg.getID().equals(g.getID()) )
        {
         found=true;
         break;
        }
       }
       
       if( ! found )
        grpIter.remove();
      }
     }
     
     if( s.getDeriverFromSamples() == null )
      proto.getDeriverFromSamples().clear();
     else
     {
      Iterator<? extends Sample> drvIter = proto.getDeriverFromSamples().iterator();
      
      while( drvIter.hasNext() )
      {
       Sample dfs = drvIter.next();
       
       boolean found = false;
       
       for( Sample sdfs : s.getDeriverFromSamples() )
       {
        if( sdfs.getID().equals(dfs.getID()) )
        {
         found=true;
         break;
        }
       }
       
       if( ! found )
        drvIter.remove();
      }
     }
 
     
    }
    
    List<ValueExtractor> extrs = new ArrayList<ValueExtractor>();
    
    if( proto.getAnnotations().size() > 0 )
    {
     AttributeInfo sampleInf = new AttributeInfo(null);
     
     collectAttributesInfo(proto, sampleInf);
     
     for( AttributeInfo ati : sampleInf.getQualifiers() )
      createAttributeExtractor(ati, extrs, null);
    }
    
    for( int k=0; k < proto.getDeriverFromSamples().size(); k++ )
     extrs.add( new DerivedFromRelationExtractor( Definitions.DERIVEDFROM ,null, k) );
    
    for( int k=0; k < proto.getGroups().size(); k++ )
     extrs.add( new GroupRelationExtractor( Definitions.BELONGSTO ,null, k) );
    
    if( extrs.size() > 0 )
    {
     out.println();
     writeHeader(extrs, out);
     writeSample( proto, extrs, out );
    }
    
    extrs.clear();
    
    AttributeInfo sampleAttrInfo = new AttributeInfo( null );
    int nGrp=0, nDFSamples=0;
 
    for( Sample s : sBlock )
    {
     collectAttributesInfo(s, sampleAttrInfo);
     
     int n = 0;
     
     for( Group sg : s.getGroups() )
     {
      if( proto.getGroup(sg.getID()) == null )
       n++;
     }
     
     if( n > nGrp )
      nGrp=n;
     
     n = 0;
     
     for( Sample dfs : s.getDeriverFromSamples())
     {
      if( proto.getDeriverFromSample(dfs.getID()) == null )
       n++;
     }
     
     if( n > nDFSamples )
      nDFSamples=n;
 
    }
    
    
    
    if( sampleAttrInfo.getQualifiers() != null )
    {
     for( AttributeInfo sAttr : sampleAttrInfo.getQualifiers() )
     {
      if( proto.getAnnotation(sAttr.getName()) == null )
       createAttributeExtractor(sAttr, extrs, null);
     }
    }
    
    for( int k=0; k < nDFSamples; k++ )
     extrs.add( new DerivedFromRelationExtractor( Definitions.DERIVEDFROM ,proto.getDeriverFromSamples(), k) );
    
    for( int k=0; k < nGrp; k++ )
     extrs.add( new GroupRelationExtractor( Definitions.BELONGSTO ,proto.getGroups(), k) );
   
    out.println();
    writeHeader(extrs, out);
 
    for( Sample s : sBlock )
     writeSample( s, extrs, out );
 
   
   }
   
  }
 
  private static void writeHeader(List<ValueExtractor> extrs, PrintStream out)
  {
   out.println();
   out.print(Definitions.SAMPLE);
 
   for( ValueExtractor extr : extrs )
   {
    out.print(TAB);
    out.print(extr.getHeader());
   }
  }
  
  private static void writeSample(Sample sample, List<ValueExtractor> extrs, PrintStream out)
  {
   out.println();
   out.print(sample.getID());
   
   for( ValueExtractor ve : extrs )
    ve.setSample(sample);
   
   boolean finished = true;
   boolean firstLine = true;
   do
   {
    if( ! firstLine )
    {
     out.println();
     out.print(TAB);
    }
    else
     firstLine = false;
    
    for( ValueExtractor ve : extrs )
    {
     out.print(TAB);
     out.print(ve.extract());
 
     if( ve.hasValue() )
      finished = false;
    }
   }
   while( ! finished );
   
  }
 
  private static List<ValueExtractor> createAttributeExtractor( AttributeInfo attr, List<ValueExtractor> resExtr, String hostTitle )
  {
   if( resExtr == null )
    resExtr = new ArrayList<ValueExtractor>();
   
   if( attr.getValuesNumber() <= MAX_INLINE_REPEATS && attr.getQualifiersNumber() == 0 && hostTitle == null )
   {
    for( int i=0; i < attr.getValuesNumber(); i++ )
     resExtr.add( new SimpleAttributeExtractor(attr.getName(), ageAttributeName(attr.getName()), i) );
   }
   else
   {
    QualifiedAttributeExtractor hostExtr = null;
    
    String title = null;
    
    if( hostTitle != null )
     hostExtr = new QualifierExtractor( attr.getName(), title = hostTitle+Definitions.QUALIFIERBRACKETS[0]+attr.getName()+Definitions.QUALIFIERBRACKETS[1] );
    else 
     hostExtr = new QualifiedAttributeExtractor( attr.getName(), title = ageAttributeName(attr.getName()) );
    
    resExtr.add( hostExtr );
   
    if( attr.getQualifiersNumber() > 0 )
    {
     for( AttributeInfo q : attr.getQualifiers() )
     {
      int sz = resExtr.size();
      
      createAttributeExtractor(q, resExtr, title);
      
      hostExtr.addQualifierExtractor( (QualifierExtractor)resExtr.get(sz) );
     }
    }
    
   }
   
   return resExtr;
  }
  
  private static void collectAttributesInfo(AnnotatedObject s, AttributeInfo parentAttrInfo )
  {
   if( s.getAnnotations() == null )
    return;
   
   for( Attribute a : s.getAnnotations() )
   {
    AttributeInfo atInf = parentAttrInfo.getInfo( a.getName() );
    
    if( atInf == null )
    {
     atInf = new AttributeInfo( a.getName() );
     
     atInf.setValuesNumber( a.getValuesNumber() );
     
     parentAttrInfo.addAttributeInfo( atInf );
    }
    else
    {
     if( a.getValuesNumber() > atInf.getValuesNumber() )
      atInf.setValuesNumber( a.getValuesNumber() );
    }
 
    collectAttributesInfo(a, atInf);
   }
   
  }
 
  private static String ageAttributeName( String name )
  {
   Matcher mtch = attrPattern.matcher(name);
   
   if( mtch.matches() )
    return mtch.group(1)+Definitions.CUSTIOMCLASSBRACKETS[0]+mtch.group(2)+Definitions.CUSTIOMCLASSBRACKETS[1];
   
   return Definitions.CUSTIOMCLASSBRACKETS[0]+name+Definitions.CUSTIOMCLASSBRACKETS[1];
  }
  
 }
