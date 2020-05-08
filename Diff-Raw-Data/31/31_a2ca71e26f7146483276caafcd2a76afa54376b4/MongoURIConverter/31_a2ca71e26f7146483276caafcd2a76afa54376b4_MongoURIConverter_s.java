 /*******************************************************************************
  * Copyright (c) 2013 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Johannes Faltermeier
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.mongodb;
 
 import org.eclipse.emf.common.util.URI;
 
 /**
  * Converts from EMFStore to mongoDB URIs.
  * <p />
  * mongoDB URIs are expected to be of form: <br />
  * mongodb://host[:port]/database/collection/{id}
  * 
  * @author jfaltermeier
  * 
  */
 public class MongoURIConverter extends AbstractESClientURIConverter {
 
 	@Override
 	protected URI normalizeWorkspaceURI(URI uri) {
		return URI.createURI(getMongoURIPrefix() + "workspace/workspace" + uri.segment(0));
 	}
 
 	@Override
 	protected URI normalizeProjectURI(URI uri) {
 		return URI.createURI(getMongoURIPrefix() + "project/" + uri.segment(3));
 	}
 
 	@Override
 	protected URI normalizeOperationsURI(URI uri) {
 		return URI.createURI(getMongoURIPrefix() + "operations/" + uri.segment(3));
 	}
 
 	@Override
 	protected URI normalizeProjectSpaceURI(URI uri) {
 		return URI.createURI(getMongoURIPrefix() + "projectspace/" + uri.segment(3));
 	}
 
 	private String getMongoURIPrefix() {
 		return "mongodb://localhost/emfstore/";
 	}
 
 }
