 package uk.ac.ebi.biosd.server.service;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import uk.ac.ebi.age.model.AgeAttribute;
 import uk.ac.ebi.age.model.AgeAttributeClass;
 import uk.ac.ebi.age.model.AgeClass;
 import uk.ac.ebi.age.model.AgeObject;
 import uk.ac.ebi.age.model.AgeRelation;
 import uk.ac.ebi.age.model.AgeRelationClass;
 import uk.ac.ebi.age.model.Attributed;
 import uk.ac.ebi.age.query.AgeQuery;
 import uk.ac.ebi.age.query.ClassNameExpression;
 import uk.ac.ebi.age.query.ClassNameExpression.ClassType;
 import uk.ac.ebi.age.storage.AgeStorage;
 import uk.ac.ebi.age.storage.DataChangeListener;
 import uk.ac.ebi.age.storage.index.AgeIndex;
 import uk.ac.ebi.age.storage.index.TextFieldExtractor;
 import uk.ac.ebi.age.storage.index.TextValueExtractor;
 import uk.ac.ebi.biosd.client.query.AttributedImprint;
 import uk.ac.ebi.biosd.client.query.AttributedObject;
 import uk.ac.ebi.biosd.client.query.GroupImprint;
 import uk.ac.ebi.biosd.client.query.Report;
 import uk.ac.ebi.biosd.client.query.SampleList;
 import uk.ac.ebi.biosd.client.shared.AttributeClassReport;
 import uk.ac.ebi.biosd.server.stat.BioSDStat;
 
 import com.pri.util.StringUtils;
 
 public class BioSDServiceImpl extends BioSDService
 {
  private AgeStorage storage;
  
  private AgeIndex groupsIndex;
  private AgeIndex samplesIndex;
  
  private AgeQuery groupSelectQuery;
  private List<AgeObject> groupList;
  
  private AgeClass sampleClass;
  private AgeClass groupClass;
  private AgeRelationClass sampleInGroupRelClass;
  private AgeRelationClass groupToSampleRelClass;
  
  private AgeRelationClass groupToPublicationRelClass;
  private AgeRelationClass groupToContactRelClass;
  
  private AgeAttributeClass desciptionAttributeClass;
  private AgeAttributeClass commentAttributeClass;
  private AgeAttributeClass dataSourceAttributeClass;
  private AgeAttributeClass referenceAttributeClass;
  
  private BioSDStat statistics;
  
  private Comparator<AgeObject> groupComparator = new Comparator<AgeObject>()
  {
   @Override
   public int compare(AgeObject o1, AgeObject o2)
   {
    Collection<? extends AgeAttribute> ref1 = o1.getAttributesByClass(referenceAttributeClass, false);
    Collection<? extends AgeAttribute> ref2 = o2.getAttributesByClass(referenceAttributeClass, false);
 
    boolean isRef1, isRef2;
    
    if( ref1 == null || ref1.size() == 0 )
     isRef1=false;
    else
    {
     AgeAttribute at = ref1.iterator().next();
     isRef1 = at.getValueAsBoolean();
    }
 
    if( ref2 == null || ref2.size() == 0 )
     isRef2=false;
    else
    {
     AgeAttribute at = ref2.iterator().next();
     isRef2 = at.getValueAsBoolean();
    }
    
    if( isRef1 == isRef2 )
     return StringUtils.naturalCompare(o1.getId(), o2.getId());
 
    return isRef1?-1:1;
    
   }
  };
  
  
  public BioSDServiceImpl( AgeStorage stor )
  {
   storage=stor;
   
   sampleClass = storage.getSemanticModel().getDefinedAgeClass( BioSDConfigManager.SAMPLE_CLASS_NAME );
   groupClass = storage.getSemanticModel().getDefinedAgeClass( BioSDConfigManager.SAMPLEGROUP_CLASS_NAME );
   sampleInGroupRelClass = storage.getSemanticModel().getDefinedAgeRelationClass( BioSDConfigManager.SAMPLEINGROUP_REL_CLASS_NAME );
   desciptionAttributeClass = storage.getSemanticModel().getDefinedAgeAttributeClass( BioSDConfigManager.DESCRIPTION_ATTR_CLASS_NAME );
   commentAttributeClass = storage.getSemanticModel().getDefinedAgeAttributeClass( BioSDConfigManager.COMMENT_ATTR_CLASS_NAME );
   dataSourceAttributeClass = storage.getSemanticModel().getDefinedAgeAttributeClass( BioSDConfigManager.DATASOURCE_ATTR_CLASS_NAME );
   referenceAttributeClass = storage.getSemanticModel().getDefinedAgeAttributeClass( BioSDConfigManager.REFERENCE_ATTR_CLASS_NAME );
   
   groupToPublicationRelClass = storage.getSemanticModel().getDefinedAgeRelationClass(BioSDConfigManager.HAS_PUBLICATION_REL_CLASS_NAME);
   groupToContactRelClass = storage.getSemanticModel().getDefinedAgeRelationClass(BioSDConfigManager.CONTACT_OF_REL_CLASS_NAME).getInverseRelationClass();
   
   if( sampleClass == null )
   {
    System.out.println("Can't find Sample class");
    return;
   }
 
   if( groupClass == null )
   {
    System.out.println("Can't find Group class");
    return;
   }
  
   if( desciptionAttributeClass == null )
   {
    System.out.println("Can't find "+BioSDConfigManager.DESCRIPTION_ATTR_CLASS_NAME+" class");
    return;
   }
  
   if( sampleInGroupRelClass == null )
   {
    System.out.println("Can't find "+BioSDConfigManager.SAMPLEINGROUP_REL_CLASS_NAME+" relation");
    return;
   }
   
   if( referenceAttributeClass == null )
   {
    System.out.println("Can't find "+BioSDConfigManager.REFERENCE_ATTR_CLASS_NAME+" attribute class");
    return;
   }
 
   groupToSampleRelClass = sampleInGroupRelClass.getInverseRelationClass();
   
 //  OrExpression orExp = new OrExpression();
 //  
 //  ClassNameExpression clsExp = new ClassNameExpression();
 //  clsExp.setClassName( ESDConfigManager.SAMPLE_CLASS_NAME );
 //  clsExp.setClassType( ClassType.DEFINED );
 //  
 //  orExp.addExpression(clsExp);
   
   ClassNameExpression clsExp = new ClassNameExpression();
   clsExp.setClassName( BioSDConfigManager.SAMPLEGROUP_CLASS_NAME );
   clsExp.setClassType( ClassType.DEFINED );
 
 //  orExp.addExpression(clsExp);
 
   
   groupSelectQuery = AgeQuery.create(clsExp);
   
   ArrayList<TextFieldExtractor> extr = new ArrayList<TextFieldExtractor>(4);
   
   extr.add( new TextFieldExtractor(BioSDConfigManager.SAMPLE_NAME_FIELD_NAME, new SampleAttrNamesExtractor() ) );
   extr.add( new TextFieldExtractor(BioSDConfigManager.SAMPLE_VALUE_FIELD_NAME, new SampleAttrValuesExtractor() ) );
   extr.add( new TextFieldExtractor(BioSDConfigManager.GROUP_NAME_FIELD_NAME, new AttrNamesExtractor() ) );
   extr.add( new TextFieldExtractor(BioSDConfigManager.GROUP_VALUE_FIELD_NAME, new AttrValuesExtractor() ) );
   extr.add( new TextFieldExtractor(BioSDConfigManager.GROUP_REFERENCE_FIELD_NAME, new RefGroupExtractor() ) );
   
   groupsIndex = storage.createTextIndex(groupSelectQuery, extr);
 
   Collection<AgeObject> grps = storage.executeQuery(groupSelectQuery);
   
   storage.addDataChangeListener( new DataChangeListener() 
   {
    @Override
    public void dataChanged()
    {
     Collection<AgeObject> grps = storage.executeQuery(groupSelectQuery);
     
     if( grps instanceof List<?> )
      groupList = (List<AgeObject>)grps;
     else
     {
      groupList = new ArrayList<AgeObject>( grps.size() );
      groupList.addAll(grps);
     }
 
     Collections.sort( groupList, groupComparator );
 
     collectStats();
    }
   } );
   
   if( grps instanceof List<?> )
    groupList = (List<AgeObject>)grps;
   else
   {
    groupList = new ArrayList<AgeObject>( grps.size() );
    groupList.addAll(grps);
   }
   
   Collections.sort( groupList, groupComparator );
   
   collectStats();
  
   clsExp = new ClassNameExpression();
   clsExp.setClassName( BioSDConfigManager.SAMPLE_CLASS_NAME );
   clsExp.setClassType( ClassType.DEFINED );
 
   AgeQuery q = AgeQuery.create(clsExp);
   
   extr = new ArrayList<TextFieldExtractor>(3);
   
   extr.add( new TextFieldExtractor(BioSDConfigManager.GROUP_ID_FIELD_NAME, new GroupIDExtractor() ) );
   extr.add( new TextFieldExtractor(BioSDConfigManager.SAMPLE_NAME_FIELD_NAME, new AttrNamesExtractor() ) );
   extr.add( new TextFieldExtractor(BioSDConfigManager.SAMPLE_VALUE_FIELD_NAME, new AttrValuesExtractor() ) );
   
   samplesIndex = storage.createTextIndex(q, extr);
  }
 
  private void collectStats()
  {
   statistics = new BioSDStat();
   
   statistics.setGroups( groupList.size() );
   
   int refGrp = 0;
   
   for( AgeObject grp : groupList )
   {
    Collection<? extends AgeAttribute> ref = grp.getAttributesByClass(referenceAttributeClass, false);
 
    boolean isRef = ref != null && ref.size() > 0 && ref.iterator().next().getValueAsBoolean();
    
    if( isRef )
     refGrp++;
    
    int samples = 0;
    for( AgeRelation rel : grp.getRelations() )
    {
     if( rel.getAgeElClass() == groupToSampleRelClass )
      samples++;
    }
    
 //   Collection<? extends AgeRelation> smpRels = grp.getRelationsByClass(groupToSampleRelClass, true);
 //   
 //   
 //   if( smpRels != null )
 //    samples = smpRels.size();
    
    statistics.addSamples( samples );
    
    if( isRef )
     statistics.addRefSamples( samples );
 
    
    String ds = grp.getAttributeValue(dataSourceAttributeClass).toString();
    
    if( ds != null )
    {
     BioSDStat dsStat = statistics.getDataSourceStat( ds );
     dsStat.addGroups(1);
     dsStat.addSamples(samples);
     
     if( isRef )
      dsStat.addRefSamples( samples );
    }
   }
   
   statistics.setRefGroups(refGrp);
  }
  
  @Override
  public Report selectSampleGroups(String query, boolean searchSmp, boolean searchGrp, boolean searchAttrNm,
    boolean searchAttrVl, boolean refOnly, int offset, int count)
  {
 //  List<AgeObject> sel = null;
   
   if( query == null )
    return getAllGroups(offset, count, refOnly);
   
   query=query.trim();
   
   if( query.length() == 0 )
    return getAllGroups(offset, count, refOnly);
   
   StringBuilder sb = new StringBuilder();
   
   sb.append("( ");
   
   if( searchAttrNm )
   {
    if( searchGrp )
     sb.append(BioSDConfigManager.GROUP_NAME_FIELD_NAME).append(":(").append(query).append(") OR ");
 
    if( searchSmp )
     sb.append(BioSDConfigManager.SAMPLE_NAME_FIELD_NAME).append(":(").append(query).append(") OR ");
   }
 
   if( searchAttrVl )
   {
    if( searchGrp )
     sb.append(BioSDConfigManager.GROUP_VALUE_FIELD_NAME).append(":(").append(query).append(") OR ");
 
    if( searchSmp )
     sb.append(BioSDConfigManager.SAMPLE_VALUE_FIELD_NAME).append(":(").append(query).append(") OR ");
   }
   
   sb.setLength(sb.length()-4);
   
   sb.append(" )");
 
  int qLen = sb.length();
   
   if( refOnly )
    sb.append(" AND ").append(BioSDConfigManager.GROUP_REFERENCE_FIELD_NAME).append(":(true)");
 
 //  sb.append(" )");
   
   String lucQuery = sb.toString();
   
 //  System.out.println("Query: "+lucQuery);
   
   List<AgeObject> sel = storage.queryTextIndex(groupsIndex, lucQuery );
   
   int nSmp = 0;
   
   for( AgeObject go : sel )
   {
    Collection<?> smps =  go.getRelationsByClass(groupToSampleRelClass, false);
    
    if( smps != null )
     nSmp+=smps.size();
   }
   
   List<GroupImprint> res = new ArrayList<GroupImprint>();
   
   int lim = offset+count;
   
   if( lim > sel.size() )
    lim=sel.size();
   
   for( int i=offset; i< lim; i++ )
   {
    GroupImprint gr = createGroupObject(sel.get(i));
    
    if( searchSmp )
    {
     sb.setLength(qLen);
    
     sb.append(" AND "+BioSDConfigManager.GROUP_ID_FIELD_NAME).append(":").append(gr.getId());
     gr.setMatchedCount( storage.queryTextIndexCount(samplesIndex, sb.toString() ) );
    }
    
    res.add(gr);
   }
   
 //  Map<AgeObject,ObjectReport> repMap = new HashMap<AgeObject, ObjectReport>();
 //  
 //  for( AgeObject obj : sel )
 //  {
 //   if( obj.getAgeElClass() == sampleClass && searchSmp )
 //   {
 //    AgeObject grpObj = getGroupForSample(obj);
 //    
 //    if( grpObj == null )
 //     continue;
 //    
 //    ObjectReport sgRep = repMap.get(grpObj);
 //    
 //    if( sgRep == null )
 //    {
 //     sgRep = createGroupObject(grpObj);
 //     
 //     repMap.put(grpObj, sgRep);
 //     res.add(sgRep);
 //    }
 //    
 //    sgRep.addMatchedSample( obj.getId() );
 //   }
 //   else if( obj.getAgeElClass() == groupClass && searchGrp )
 //   {
 //    ObjectReport sgRep = repMap.get(obj);
 //    
 //    if( sgRep == null )
 //    {
 //     sgRep = createGroupObject(obj);
 //
 //     repMap.put(obj, sgRep);
 //     res.add(sgRep);
 //    }
 //   }
 //  }
   Report rep = new Report();
   rep.setObjects(res);
   rep.setTotalGroups(sel.size());
   rep.setTotalSamples(nSmp);
   
   return rep;
  }
  
  private GroupImprint createGroupObject( AgeObject obj )
  {
   GroupImprint sgRep = new GroupImprint();
 
   sgRep.setId( obj.getId() );
 
 //  sgRep.addAttribute("Submission ID", obj.getSubmission().getId(), true, 0);
   sgRep.addAttribute("ID", obj.getId(), true, 0);
   
   Object descVal = obj.getAttributeValue(desciptionAttributeClass);
   sgRep.setDescription( descVal!=null?descVal.toString():null );
   
  
   for( AgeAttribute atr : obj.getAttributes() )
   {
    AgeAttributeClass atCls = atr.getAgeElClass();
    
    if( atCls.isClassOrSubclass( commentAttributeClass ) )
    {
     sgRep.addOtherInfo( atCls.getName(), atr.getValue().toString() );
    }
    else
     sgRep.addAttribute(atCls.getName(), atr.getValue().toString(), atr.getAgeElClass().isCustom(),atr.getOrder());
   }
   
   Collection<? extends AgeRelation> pubRels =  obj.getRelationsByClass(groupToPublicationRelClass, false);
 
   if( pubRels != null )
   {
    for( AgeRelation pRel : pubRels )
     sgRep.addPublication( createAttributedObject(pRel.getTargetObject()) );
   }
 
   Collection<? extends AgeRelation> persRels =  obj.getRelationsByClass(groupToContactRelClass, false);
 
   if( persRels != null )
   {
    for( AgeRelation pRel : persRels )
     sgRep.addContact( createAttributedObject(pRel.getTargetObject()) );
   }
 
   
   Collection<? extends AgeRelation> rels =  obj.getRelationsByClass(groupToSampleRelClass, false);
   
   int sCount=0;
   if( rels != null )
   {
    for( AgeRelation rl : rels )
     if( rl.getTargetObject().getAgeElClass() == sampleClass )
      sCount++;
   }
   
   sgRep.setRefCount( sCount );
   
   return sgRep;
  }
 
  private AttributedImprint createAttributedObject(AgeObject ageObj)
  {
   AttributedImprint obj = new AttributedImprint();
   
   if( ageObj.getAttributes() != null )
   {
    for( AgeAttribute attr : ageObj.getAttributes() )
    {
     obj.addAttribute(attr.getAgeElClass().getName(), attr.getValue().toString(), attr.getAgeElClass().isCustom(), attr.getOrder());
    }
   }
   
   return obj;
  }
  
 // private AgeObject getGroupForSample(AgeObject obj)
 // {
 //  for(AgeRelation rel : obj.getRelations() )
 //  {
 //   if( rel.getAgeElClass() == sampleInGroupRelClass && rel.getTargetObject().getAgeElClass() == groupClass )
 //    return rel.getTargetObject();
 //  }
 //  
 //  return null;
 // }
 
 
 
  public void shutdown()
  {
   storage.shutdown();
  }
  
  class AttrValuesExtractor implements TextValueExtractor
  {
   StringBuilder sb = new StringBuilder();
  
   public String getValue(AgeObject gobj)
   {
    sb.setLength(0);
    
    for( AgeAttribute attr : gobj.getAttributes() )
    {
     Object val = attr.getValue();
     
     if( val instanceof String )
      sb.append( val ).append(' ');
     
     if( attr.getAttributes() != null )
     {
      for( AgeAttribute qual : attr.getAttributes() )
      {
       Object qval = qual.getValue();
       
       if( qval instanceof String )
        sb.append( qval ).append(' ');
      }
     }
 
    }
     
    return sb.toString();
   }
  }
  
  class SampleAttrValuesExtractor implements TextValueExtractor
  {
   StringBuilder sb = new StringBuilder();
  
   public String getValue(AgeObject gobj)
   {
    sb.setLength(0);
 
    for(AgeRelation rel : gobj.getRelations())
    {
     if(rel.getAgeElClass() == groupToSampleRelClass)
     {
      AgeObject obj = rel.getTargetObject();
 
      for(AgeAttribute attr : obj.getAttributes())
      {
       Object val = attr.getValue();
       
       if( val instanceof String )
        sb.append( val ).append(' ');
 
       if(attr.getAttributes() != null)
       {
        for(AgeAttribute qual : attr.getAttributes() )
        {
         Object qval = qual.getValue();
         
         if( qval instanceof String )
          sb.append( qval ).append(' ');
        }
       }
      }
     }
    }
    return sb.toString();
   }
  }
  
  class RefGroupExtractor implements TextValueExtractor
  {
   StringBuilder sb = new StringBuilder();
 
   public String getValue(AgeObject gobj)
   {
    for( AgeAttribute attr : gobj.getAttributes() )
    {
     if( attr.getAgeElClass() == referenceAttributeClass && attr.getValueAsBoolean() )
      return "true";
    }
    
    return "";
   }
  }
  
  class SampleAttrNamesExtractor implements TextValueExtractor
  {
   StringBuilder sb = new StringBuilder();
 
   public String getValue(AgeObject gobj)
   {
    sb.setLength(0);
 
    for(AgeRelation rel : gobj.getRelations())
    {
     if(rel.getAgeElClass() == groupToSampleRelClass)
     {
      AgeObject obj = rel.getTargetObject();
 
      for(AgeAttribute attr : obj.getAttributes())
      {
       sb.append(attr.getAgeElClass().getName()).append(' ');
 
       if(attr.getAttributes() != null)
       {
        for(AgeAttribute qual : attr.getAttributes())
        {
         sb.append(qual.getAgeElClass().getName()).append(' ');
        }
       }
      }
     }
    }
 
    return sb.toString();
   }
  }
 
  class AttrNamesExtractor implements TextValueExtractor
  {
   StringBuilder sb = new StringBuilder();
  
   public String getValue(AgeObject gobj)
   {
    sb.setLength(0);
    
    for( AgeAttribute attr : gobj.getAttributes() )
    {
     sb.append( attr.getAgeElClass().getName() ).append(' ');
     
     if( attr.getAttributes() != null )
     {
      for( AgeAttribute qual : attr.getAttributes() )
      {
       sb.append( qual.getAgeElClass().getName() ).append(' ');
      }
     }
    }
     
    return sb.toString();
   }
  }
  
  class GroupIDExtractor implements TextValueExtractor
  {
  
   public String getValue(AgeObject gobj)
   {
    for(AgeRelation rel : gobj.getRelations())
    {
     if(rel.getAgeElClass() == sampleInGroupRelClass)
     {
      return rel.getTargetObject().getId();
     }
    }
    return "";
   }
  }
 
  
  private SampleList createSampleReport(List<AgeObject> samples)
  {
   int id=1;
   
   SampleList sl = new SampleList();
   
   Map<AgeAttributeClass, AttributeClassReport > valMap = new HashMap<AgeAttributeClass, AttributeClassReport>();
   
   for( AgeObject smpl : samples )
   {
    sl.addSample( convertAttributed(smpl) );
 
    
 //   List<AttributedObject> clSmpl = new ArrayList<AttributedObject>();
    
 //   Map<String,String> attrMap = new HashMap<String, String>(); 
    
 //   clSmpl.add( new AttributedObject("__id",smpl.getId()) );
    
 //   attrMap.put("__id",smpl.getId());
    
    for( AgeAttribute attr : smpl.getAttributes() )
    {
     AgeAttributeClass ageAtCls = attr.getAgeElClass();
     String attrval = attr.getValue().toString();
     
     AttributeClassReport atCls = valMap.get(ageAtCls);
     
     if( atCls == null )
     {
      atCls = new AttributeClassReport();
      
      atCls.setCustom( ageAtCls.isCustom() );
      atCls.setName( ageAtCls.getName() );
 //     atCls.setId("AttrClass"+(id++));
      atCls.setId((ageAtCls.isCustom()?"CC:":"DC:")+ageAtCls.getName());
      
      valMap.put(attr.getAgeElClass(), atCls);
     }
     
     atCls.addValue(attrval);
     
 //    AttributedObject a = convertAttributed(obj);
 //    
 //    if( attr.getAttributes() != null )
 //     collectQualifiers(attr,a);
 //    
 //    clSmpl.add( a );
    }
    
 //   sl.addSample( clSmpl );
   }
   
   List<AttributeClassReport> clsLst = new ArrayList<AttributeClassReport>( valMap.size()+1 );
   clsLst.addAll(valMap.values());
   
   Collections.sort(clsLst, new Comparator<AttributeClassReport>()
   {
    @Override
    public int compare(AttributeClassReport arg0, AttributeClassReport arg1)
    {
     if( arg0.getName().equals("Name") )
     {
      if( arg1.getName().equals("Name") )
       return 0;
      else
       return -1;
     }
     else if( arg1.getName().equals("Name") )
      return 1;
     
     return arg1.getValues().size()-arg0.getValues().size();
    }
   });
   
   for( AttributeClassReport atr : clsLst )
    atr.clearValues();
   
   AttributeClassReport idCls = new AttributeClassReport();
   
   idCls.setCustom(false);
   idCls.setName("ID");
   idCls.setId("__id");
   
   clsLst.add(idCls);
   
   sl.setHeader( clsLst );
   
   return sl;
  }
 
  private AttributedObject convertAttributed( Attributed obj )
  {
   AttributedObject objAttr = new AttributedObject();
 
   if( obj instanceof AgeObject )
   {
    objAttr.setName( ((AgeObject) obj).getId() );
    
    if( obj.getAttributes() != null )
    {
     List<AttributedObject> attrs = new ArrayList<AttributedObject>();
     
     for( Attributed oa : obj.getAttributes() )
      attrs.add(convertAttributed(oa));
     
     objAttr.setAttributes(attrs);
    }
   }
   else if( obj instanceof AgeAttribute )
   {
    AgeAttribute ageAt = (AgeAttribute)obj;
    
    objAttr.setName((ageAt.getAgeElClass().isCustom()?"CC:":"DC:")+ageAt.getAgeElClass().getName());
    
    Object val = ageAt.getValue();
    
    if( val instanceof Attributed )
     objAttr.setObjectValue( convertAttributed( (Attributed)val ) );
    else
     objAttr.setStringValue(val.toString());
    
    if( ageAt.getAttributes() != null )
    {
     List<AttributedObject> attrs = new ArrayList<AttributedObject>();
     
     for( Attributed oa : ageAt.getAttributes() )
      attrs.add(convertAttributed(oa));
     
     objAttr.setAttributes(attrs);
    }
 
   }
   
   return objAttr;
  }
  
 // private void collectQualifiers(AgeAttribute ageAttr, AttributedObject biosdAttr)
 // {
 //  List<AttributedObject> bioQl = new ArrayList<AttributedObject>( 3 );
 //  
 //  for( AgeAttribute q : ageAttr.getAttributes() )
 //  {
 //   AttributedObject bq = new AttributedObject( q.getAgeElClass().getName(), q.getValue().toString() );
 //   
 //   if( q.getAttributes() != null )
 //    collectQualifiers(q,bq);
 //   
 //   bioQl.add(bq);
 //  }
 //  
 //  biosdAttr.setQualifiers(bioQl);
 // }
  
  
  @Override
  public SampleList getSamplesByGroup(String grpID, int offset, int count)
  {
   AgeObject grpObj = storage.getObjectById(grpID);
   
   if( grpObj == null )
    return null;
   
   List<AgeObject> res = new ArrayList<AgeObject>(count);
   
   int total=0;
   
   for( AgeRelation rel : grpObj.getRelations() )
   {
    if( rel.getAgeElClass() == groupToSampleRelClass )
    {
     total++;
     
     if( offset > 0 )
     {
      offset--;
      continue;
     }
  
     if( count == 0 )
      continue;
    
     count--;
     
     res.add(rel.getTargetObject());
     
    }
   }
   
   SampleList sl = createSampleReport(res);
   sl.setTotalRecords(total);
   
   return sl;
  }
  
  public Report getSamplesByGroupX(String grpID, int offset, int count)
  {
   AgeObject grpObj = storage.getObjectById(grpID);
   
   if( grpObj == null )
    return null;
   
   List<GroupImprint> res = new ArrayList<GroupImprint>(30);
   
   int total=0;
   
   for( AgeRelation rel : grpObj.getRelations() )
   {
    if( rel.getAgeElClass() == groupToSampleRelClass )
    {
     total++;
     
     if( offset > 0 )
     {
      offset--;
      continue;
     }
  
     if( count == 0 )
      continue;
    
     count--;
     
     AgeObject sample = rel.getTargetObject();
     
     GroupImprint rp = new GroupImprint();
     
     rp.setId(sample.getId());
     
     
     for( AgeAttribute attr : sample.getAttributes() )
     {
      String attrname = attr.getAgeElClass().getName();
      
      rp.addAttribute( attrname, attr.getValue().toString(), attr.getAgeElClass().isCustom(),attr.getOrder());
      
 //     if( attr.getAttributes() != null )
 //     {
 //      for( AgeAttribute qlf  : attr.getAttributes() )
 //       rp.addAttribute( attrname+"["+qlf.getAgeElClass().getName()+"]", qlf.getValue().toString(), attr.getAgeElClass().isCustom(), qlf.getOrder());
 //     }
     }
 
     rp.addAttribute( "ID", sample.getId(), true, 0);
     
     res.add( rp );
    }
   }
   
   Report rep = new Report();
   rep.setObjects(res);
   rep.setTotalGroups(total);
   
   return rep;
  }
 
  
  @Override
  public SampleList getSamplesByGroupAndQuery(String grpId, String query, boolean searchAttrNm, boolean searchAttrVl, int offset, int count)
  {
   StringBuilder sb = new StringBuilder();
   
   sb.append("( ");
   
   if( searchAttrNm )
    sb.append(BioSDConfigManager.SAMPLE_NAME_FIELD_NAME).append(":(").append(query).append(')');
 
   if( searchAttrVl )
   {
    if( searchAttrNm )
     sb.append(" OR ");
    
    sb.append(BioSDConfigManager.SAMPLE_VALUE_FIELD_NAME).append(":(").append(query).append(')');
   }
   
   sb.append(" ) AND ").append(BioSDConfigManager.GROUP_ID_FIELD_NAME).append(":").append(grpId);
   
   List<AgeObject> sel = storage.queryTextIndex(samplesIndex, sb.toString() );
   
  
   if( offset > sel.size())
    return null;
   
   int end = (offset+count) > sel.size()? sel.size() : (offset+count); 
   
   SampleList lst = createSampleReport( sel.subList(offset, end) );
   lst.setTotalRecords(sel.size());
   
   return lst;
  }
  
  public Report getSamplesByGroupAndQueryX(String grpId, String query, boolean searchAttrNm, boolean searchAttrVl, int offset, int count)
  {
   StringBuilder sb = new StringBuilder();
   
   sb.append("( ");
   
   if( searchAttrNm )
    sb.append(BioSDConfigManager.SAMPLE_NAME_FIELD_NAME).append(":(").append(query).append(')');
 
   if( searchAttrVl )
   {
    if( sb.length() > 0 )
     sb.append(" OR ");
    
    sb.append(BioSDConfigManager.SAMPLE_VALUE_FIELD_NAME).append(":(").append(query).append(')');
   }
   
   sb.append(" ) AND ").append(BioSDConfigManager.GROUP_ID_FIELD_NAME).append(":").append(grpId);
   
   List<AgeObject> sel = storage.queryTextIndex(samplesIndex, sb.toString() );
   
   List<GroupImprint> res = new ArrayList<GroupImprint>();
  
   int total = res.size();
   
   for( AgeObject obj : sel )
   {
    if( offset > 0 )
    {
     offset--;
     continue;
    }
 
    if( count == 0 )
     continue;
   
    count--;
 
    GroupImprint sampRep = new GroupImprint();
 
    sampRep.setId( obj.getId() );
 
    sampRep.addAttribute("ID", obj.getId(), true, 0);
    
    Object descVal = obj.getAttributeValue(desciptionAttributeClass);
    sampRep.setDescription( descVal!=null?descVal.toString():null );
    
    for( AgeAttribute atr : obj.getAttributes() )
    {
     String attrname = atr.getAgeElClass().getName();
 
     sampRep.addAttribute(attrname, atr.getValue().toString(), atr.getAgeElClass().isCustom(),atr.getOrder());
 
     if( atr.getAttributes() != null )
     {
      for( AgeAttribute qlf  : atr.getAttributes() )
       sampRep.addAttribute( attrname+"["+qlf.getAgeElClass().getName()+"]", qlf.getValue().toString(), atr.getAgeElClass().isCustom(), qlf.getOrder());
     }
 
    }
    res.add(sampRep);
   }
   
 //  for( AgeObject obj : sel )
 //  {
 //   if( obj.getAgeElClass() == sampleClass )
 //   {
 //    AgeObject grpObj = getGroupForSample(obj);
 //    
 //    if( grpObj == null || ! grpObj.getId().equals(grpId)  )
 //     continue;
 //    
 //    ObjectReport sampRep = new ObjectReport();
 //
 //     sampRep.setId( obj.getId() );
 //
 //     sampRep.addAttribute("ID", obj.getId(), true, 0);
 //     
 //     Object descVal = obj.getAttributeValue(desciptionAttributeClass);
 //     sampRep.setDescription( descVal!=null?descVal.toString():null );
 //     
 //     for( AgeAttribute atr : obj.getAttributes() )
 //     {
 //      String attrname = atr.getAgeElClass().getName();
 //
 //      sampRep.addAttribute(attrname, atr.getValue().toString(), atr.getAgeElClass().isCustom(),atr.getOrder());
 //
 //      if( atr.getQualifiers() != null )
 //      {
 //       for( AgeAttribute qlf  : atr.getQualifiers() )
 //        sampRep.addAttribute( attrname+"["+qlf.getAgeElClass().getName()+"]", qlf.getValue().toString(), atr.getAgeElClass().isCustom(), qlf.getOrder());
 //      }
 //
 //     }
 //     res.add(sampRep);
 //   }
 //  }
   Report rep = new Report();
   rep.setObjects(res);
   rep.setTotalGroups(total);
   
   return rep;
  }
 
  @Override
  public Report getAllGroups(int offset, int count, boolean refOnly )
  {
   int lim = offset+count;
   
   int last = refOnly?statistics.getRefGroups():groupList.size();
   
   if( lim > last )
    lim=last;
   
   List<GroupImprint> res = new ArrayList<GroupImprint>(count);
   
   for( ; offset < lim; offset++)
    res.add( createGroupObject(groupList.get(offset)) );
   
   Report rep = new Report();
   rep.setObjects(res);
   rep.setTotalGroups(refOnly?statistics.getRefGroups():statistics.getGroups());
   rep.setTotalSamples(refOnly?statistics.getRefSamples():statistics.getSamples());
   
   return rep;
  }
 
  @Override
  public BioSDStat getStatistics()
  {
   return statistics;
  }
 
 
 }
