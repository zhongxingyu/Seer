 /**
  * element that holds a path = connection between two knots
  */
 
 
 import processing.core.*;
 import processing.data.XML;
 
 class Path extends SceneElement {
   
   Point m_start;
   Point m_end;
   
   Knot m_knot_from;
   Knot m_knot_to;
   
   String m_from_id;
   String m_to_id;
 
   int m_stroke_width;  //!< diameter of the path  
   
   public Path(PApplet parent, XML xml, IdResolver resolver) throws Exception {
     
     super(parent, xml, resolver);
     
   }
   
   public boolean goesFromTo(String from_id, String to_id) {
     return m_from_id.equals(from_id) && m_to_id.equals(to_id);
   }
   
   void parse_element_attributes(XML xml) throws Exception {
     
     m_from_id = xml.getString("from", "").trim().toLowerCase();
     m_to_id = xml.getString("to", "").trim().toLowerCase();
     
     if (m_from_id.length() == 0 || m_to_id.length() == 0 ) {
       throw new Exception("missing from or to for path with id "  +  getId());        
     }
       
     m_stroke_width = xml.getInt("width", 1);
     
   }
   
   void initElement() throws Exception {
     
     if (null == (m_knot_from = m_resolver.getKnot(m_from_id))) {      
       throw new Exception("invalid start knot given for path with id " + getId());
     }
     m_start = m_knot_from.m_pos;
     
     
     if (null == (m_knot_to = m_resolver.getKnot(m_to_id))) {
       throw new Exception("invalid end knot given for path with id " + getId());
     }
     m_end = m_knot_to.m_pos;
     
     m_width = 0;
     if (m_label_position != 0) {
       throw new Exception("invalid label position given for path  " + getId() + ". only center position allowed");      
     }
     
     m_top_middle_pos = new Point( m_start.getX() + ((m_end.getX() - m_start.getX())/2), 
                                   m_start.getY() + ((m_end.getY() - m_start.getY())/2),
                                   m_start.getZ() + ((m_end.getZ() - m_start.getZ())/2));
                                                                      
   }
   
   public void drawSpecificPart(int time) {
     
     p.strokeWeight(m_stroke_width);
     p.stroke(m_color);
     p.line(m_start.getX(), m_start.getY(), m_start.getZ(),
                   m_end.getX(), m_end.getY(), m_end.getZ());
     p.noStroke();         
   }
   
   int getElementWidth() { return m_width; }
   Point getElementTopMiddlePosition() { return m_top_middle_pos; }
 
   boolean parentsVisible(int time) {
     return m_knot_from.isActive(time) && m_knot_to.isActive(time);
   }  
 }
