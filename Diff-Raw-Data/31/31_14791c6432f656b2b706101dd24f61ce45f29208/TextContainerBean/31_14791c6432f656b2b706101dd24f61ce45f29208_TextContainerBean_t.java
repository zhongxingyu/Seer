 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.steps.xliffkit.common.persistence.beans;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.annotation.IAnnotation;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.steps.xliffkit.common.persistence.FactoryBean;
 import net.sf.okapi.steps.xliffkit.common.persistence.IPersistenceBean;
 import net.sf.okapi.steps.xliffkit.common.persistence.IPersistenceSession;
 
 public class TextContainerBean extends TextFragmentBean {
 	
 	private List<PropertyBean> properties = new ArrayList<PropertyBean>();
 	private List<FactoryBean> annotations = new ArrayList<FactoryBean>();
 	private List<SegmentBean> segments = new ArrayList<SegmentBean>();
 	private IPersistenceSession session;
 
 	@Override
 	public void init(IPersistenceSession session) {		
 		this.session = session;
 	}
 	
 	@Override
 	public <T> T get(Class<T> classRef) {		
 		
 		TextContainer tc = null;
 		
 		if (session == null)
 			tc = new TextContainer();
 		else
 			tc = session.convert(this, TextContainer.class); // Get an object with superclass fields set		
 		
 		for (PropertyBean prop : properties)
 			tc.setProperty(prop.get(Property.class));
 		
 		for (FactoryBean annotationBean : annotations)
 			tc.setAnnotation(annotationBean.get(IAnnotation.class));
 		
		// TODO get segments
//		for (SegmentBean segment : segments)
//			tc.getSegments().add(segment.get(Segment.class));
 		
 		return classRef.cast(tc);
 	}
 	
 	@Override
 	public IPersistenceBean set(Object obj) {
 		super.set(obj);
 		
 		if (obj instanceof TextContainer) {
 			TextContainer tc = (TextContainer) obj;
 						
 			for (String propName : tc.getPropertyNames()) {
 				PropertyBean propBean = new PropertyBean();
 				propBean.set(tc.getProperty(propName));
 				properties.add(propBean);
 			}
 			
 			// TODO TextUnit.getAnnotations()
 			//annotations.write(tu.ge)
 			
			// TODO set segments
//			List<Segment> segs = tc.getSegments();
//			if (segs != null)
//				for (Segment segment : segs) {
//					SegmentBean segBean = new SegmentBean();
//					segments.add(segBean);
//					segBean.set(segment);
//				}
 			
 		}		
 		return this;
 	}
 
 	public List<SegmentBean> getSegments() {
 		return segments;
 	}
 
 	public void setSegments(List<SegmentBean> segments) {
 		this.segments = segments;
 	}
 
 	public void setAnnotations(List<FactoryBean> annotations) {
 		this.annotations = annotations;
 	}
 
 	public List<FactoryBean> getAnnotations() {
 		return annotations;
 	}
 
 	public List<PropertyBean> getProperties() {
 		return properties;
 	}
 
 	public void setProperties(List<PropertyBean> properties) {
 		this.properties = properties;
 	}
 	
 }
