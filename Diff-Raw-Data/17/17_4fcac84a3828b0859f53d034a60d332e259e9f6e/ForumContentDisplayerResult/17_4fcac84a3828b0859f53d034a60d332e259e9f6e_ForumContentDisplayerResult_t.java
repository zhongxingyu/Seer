 /*
  * Copyright 2013 :torweg free software group
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package org.torweg.pulse.component.forum;
 
 import java.util.List;
 
 import org.jdom.Element;
 import org.torweg.pulse.service.request.ServiceRequest;
 import org.torweg.pulse.site.content.Content;
 import org.torweg.pulse.site.content.ContentGroup;
 import org.torweg.pulse.site.content.ContentResult;
 import org.torweg.pulse.site.map.SitemapNode;
 /**
  * @author Thomas Weber, Zach Metcalf
  * @version $Revision$
  */
 public class ForumContentDisplayerResult extends ContentResult {
 	
	public ForumContentDisplayerResult(final Content c, final ServiceRequest r) {
 		super(c, r);
 	}
 	
	public ForumContentDisplayerResult(final ContentGroup co,
 			final List<SitemapNode> ch, final ServiceRequest r) {
 		super(co, ch, r);
 	}
 	
 	@Override
 	public Element deserializeToJDOM(final ServiceRequest r) {
 		Element jdom = super.deserializeToJDOM(r);
 		/* add children? */
 		deserializeChildren(jdom);
 		return jdom;
 	}
 }
