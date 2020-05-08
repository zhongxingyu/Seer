 package uk.ac.ebi.biosd.client.ui.test;
 
 import uk.ac.ebi.biosd.client.query.AttributedObject;
 
 import com.smartgwt.client.widgets.HTMLFlow;
 
 public class ObjectViewPanelHTML extends HTMLFlow
 {
  public ObjectViewPanelHTML( AttributedObject obj )
  {
   String html="<table class='qualifierTable'>";
   
   
   for( AttributedObject at : obj.getAttributes() )
   {
    html += "<tr>";
    
    String rowSpan ="";
    
    if(  at.getObjectValue() != null  )
     rowSpan = " rowspan='"+(at.getObjectValue().getAttributes().size()+1)+"'";
    
    html += "<td class='qualifierName'"+rowSpan+">"+at.getName()+":&nbsp</td>";
    
    String val = at.getStringValue();
    
    if(  at.getObjectValue() != null  )
     html += "<td class='qualifierObjAttrName'>Name:&nbsp</td><td class='qualifierValue'>"+val.substring(val.indexOf('-')+1)+"</td>";
    else
     html += "<td colspan='2' style='qualifierValue'>"+val+"</td>";
    
    html += "</tr>";
 
    if( at.getObjectValue() != null )
    {
     for( AttributedObject obat : at.getObjectValue().getAttributes() )
     {
     html += "<tr><td class='qualifierObjAttrName'>"+obat.getName()+":&nbsp</td>";
      
      val = obat.getStringValue();
      
      if( val.startsWith("http://") )
       val = "<a target='_blank' href='"+val+"'>"+val+"</a>";
      
     html += "<td class='qualifierValue'>"+val+"</td></tr>";
     }
    }
    
 
   }
   
   html+="</table>";
   
   setContents(html);
  }
 }
