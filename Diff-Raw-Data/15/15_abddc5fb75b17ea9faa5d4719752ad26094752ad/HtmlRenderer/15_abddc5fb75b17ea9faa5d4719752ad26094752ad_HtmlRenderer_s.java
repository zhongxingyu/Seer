 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is Gmodel.
  *
  * The Initial Developer of the Original Code is
  * Sofismo AG (Sofismo).
  * Portions created by the Initial Developer are
  * Copyright (C) 2009-2010 Sofismo AG.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Jorn Bettin
  * Chul Kim
  * Andrew Shewring
  * ***** END LICENSE BLOCK ***** */
 
 package org.s23m.cell.eclipse.visualization.graph.rendering;
 
 import java.util.Arrays;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTError;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.browser.LocationAdapter;
 import org.eclipse.swt.browser.LocationEvent;
 import org.eclipse.swt.widgets.Composite;
 import org.s23m.cell.G;
 import org.s23m.cell.Set;
 import org.s23m.cell.api.Instantiation;
 import org.s23m.cell.api.models.HTMLRepresentation;
 import org.s23m.cell.api.models2.RepositoryStructure;
 import org.s23m.cell.xtend2.HtmlTemplateTransformation;
 
 public final class HtmlRenderer extends AbstractRenderer {
 
	private static final String LOCATION_PREFIX = "about:";
 
 	private static final String NO_LOCATION = "about:blank";
 
 	private Browser browser;
 
 	public HtmlRenderer(final RenderingCoordinator coordinator, final Composite parentContainer) {
 		super(coordinator);
 
 		try {
 			browser = new Browser(parentContainer, SWT.NONE);
 		} catch (final SWTError e) {
 			System.out.println("Could not instantiate Browser: " + e.getMessage());
 			return;
 		}
 
 		browser.addLocationListener(new LocationAdapter() {
 		    @Override
 			public void changing(final LocationEvent event) {
 		    	// assume all hyperlinks are symbolic and refer to other Sets
 				final String location = event.location;
 				if (!NO_LOCATION.equals(location)) {
					final String identifier = location.substring(LOCATION_PREFIX.length());
 
 					// find referenced Set
 					final Set referencedSet = findByIdentifier(identifier);
 					if (referencedSet != null) {
 						updateSetToRender(referencedSet);
 						render();
 					}
 					// prevent normal link-following behaviour
 					event.doit = false;
 				}
 		    }
 		});
 	}
 
 	private Set findByIdentifier(final String identifier) {
 		final Set setToRender = getSetToRender();
 		for (final Set set : setToRender.filterFlavor(G.coreGraphs.vertex)) {
 			if (identifier.equals(set.identity().identifier().toString())) {
 				return set;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void doRender() {
 		//final OawHtmlDerivedFileGenerator generator = new OawHtmlDerivedFileGenerator();
 		/*
 		 * TODO retrieve template content eventually from
 		 *
 		 * HTMLRepresentation.html_to_artifact.identity().getPayload()
 		 *
 		 * (needs to be set to fixed HTML template as part of boot sequence)
 		 */
 		final Set setToRender = getSetToRender();
 		final HtmlTemplateTransformation htmlTransformation = new HtmlTemplateTransformation();
 		final String generatedOutput = htmlTransformation.apply(Arrays.asList(setToRender)).toString();
 
 		final String identityName = HTMLRepresentation.htmlRepresentation.identity().name() + " of " + setToRender.identity().name();
 		final Set semanticIdentity = Instantiation.addDisjunctSemanticIdentitySet(identityName, identityName, RepositoryStructure.htmlRepresentations);
 		semanticIdentity.identity().setPayload(generatedOutput);
 
 		// create htmlRepresentation instance whose identity contains the generated HTML
 		Instantiation.instantiateConcrete(HTMLRepresentation.htmlRepresentation, semanticIdentity);
 
 		// show in browser
 		browser.setText(generatedOutput);
 	}
 
 }
