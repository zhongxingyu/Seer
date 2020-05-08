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
 package org.torweg.pulse.component.forum.model;
 
 import javax.persistence.Entity;
 import javax.xml.bind.annotation.XmlAccessOrder;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorOrder;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.torweg.pulse.bundle.ExtendedJDOMable;
 import org.torweg.pulse.site.content.AbstractBasicContent;
 
 /**
  * @author Thomas Weber, Daniel Dietz, Zach Metcalf
  * @version $Revision$
  */
 @Entity
 @XmlRootElement(name = "forum-content")
 @XmlAccessorOrder(XmlAccessOrder.UNDEFINED)
 @XmlAccessorType(XmlAccessType.FIELD)
public class ForumContent extends AbstractBasicContent implements
 		ExtendedJDOMable {
 	
 	/**
 	 * the logger for {@code ForumContent}.
 	 */
 	private static final Logger LOGGER = LoggerFactory
			.getLogger(ForumContent.class);
 	
 }
