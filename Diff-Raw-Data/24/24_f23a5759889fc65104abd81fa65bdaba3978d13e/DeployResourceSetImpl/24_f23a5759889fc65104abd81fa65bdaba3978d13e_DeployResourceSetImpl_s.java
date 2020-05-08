 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.eclipse.bpel.apache.ode.deploy.ui.util;
 
 import org.eclipse.bpel.model.resource.BPELResourceSetImpl;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 
 /**
  * @author Bob Brodt (bbrodt@redhat.com)
  * @date Aug 31, 2010
  * @see Bugzilla 324164
  */
 
 @SuppressWarnings("nls")
 public class DeployResourceSetImpl extends BPELResourceSetImpl {
 
 	public DeployResourceSetImpl() {
 		super();
 	}
 
 	@Override
 	public Resource getResource(URI uri, boolean loadOnDemand) {
 		String name = uri.toString().toLowerCase();
 		// revert https://jira.jboss.org/browse/JBIDE-6825
 		if (name.endsWith("wsdl"))
 			return getResource(uri,true,"wsdl");
 		if (name.endsWith("wsil"))
 			return getResource(uri,true,"wsil");
 		if (name.endsWith(".xsd"))
 			return getResource(uri,true,"xsd");
 		if (name.endsWith(".bpel"))
 			return getResource(uri,true,"bpel");
 		return super.getResource(uri,loadOnDemand);
 	}
 }
