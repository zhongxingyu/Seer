 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  */
 
 package com.frovi.ss.tests;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.exception.ConstraintException;
 import org.infoglue.cms.exception.SystemException;
 
 /**
  * @author ss
  *
  * To change the template for this generated type comment go to
  * Window>Preferences>Java>Code Generation>Code and Comments
  */
 public class testRecursive {
 
 	public static void main(String[] args) throws ConstraintException, SystemException {
		List l = ContentVersionController.getContentVersionController().getContentVersionVOWithParentRecursive(new Integer(84),new Integer(0), true);
 		
 		Iterator it = l.iterator();
 		ContentVO contentVO = null;
 		ContentVersionVO contentVersionVO; 
 		
 		while (it.hasNext())
 		{
 			Object o = it.next();
 
 			contentVersionVO = (ContentVersionVO) o;
 			String cName = "N/A";
 			cName = contentVersionVO.getContentName();
 			
 			String out = cName + " - " + 
 			contentVersionVO.getLanguageName()
 				+ "(state:" + contentVersionVO.getStateId() + " isactive: " + contentVersionVO.getIsActive() + ")";
 				
 			System.out.println(out);
 				 
 		}
 		
 		
 	}
 }
