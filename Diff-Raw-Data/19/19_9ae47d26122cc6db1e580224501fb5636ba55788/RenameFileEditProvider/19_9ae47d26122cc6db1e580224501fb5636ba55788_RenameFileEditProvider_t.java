 /***** BEGIN LICENSE BLOCK *****
  * Version: CPL 1.0/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Common Public
  * License Version 1.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.eclipse.org/legal/cpl-v10.html
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * Copyright (C) 2007 Mirko Stocker <me@misto.ch>
  * 
  * Alternatively, the contents of this file may be used under the terms of
  * either of the GNU General Public License Version 2 or later (the "GPL"),
  * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the CPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the CPL, the GPL or the LGPL.
  ***** END LICENSE BLOCK *****/
 
 package org.rubypeople.rdt.refactoring.core.renamefile;
 
 import java.util.Collection;
 
 import org.jruby.ast.Node;
import org.jruby.util.ByteList;
 import org.rubypeople.rdt.refactoring.core.NodeProvider;
 import org.rubypeople.rdt.refactoring.editprovider.FileEditProvider;
 import org.rubypeople.rdt.refactoring.editprovider.FileMultiEditProvider;
 import org.rubypeople.rdt.refactoring.editprovider.IMultiFileEditProvider;
 import org.rubypeople.rdt.refactoring.editprovider.MultiFileEditProvider;
 import org.rubypeople.rdt.refactoring.editprovider.SimpleNodeEditProvider;
 import org.rubypeople.rdt.refactoring.nodewrapper.RequireAndLoadWrapper;
 import org.rubypeople.rdt.refactoring.util.NameHelper;
 
 public class RenameFileEditProvider implements IMultiFileEditProvider {
 
 	private final RenameFileConfig config;
 
 	public RenameFileEditProvider(RenameFileConfig config) {
 		this.config = config;
 	}
 
 	public Collection<FileMultiEditProvider> getFileEditProviders() {
 		MultiFileEditProvider fileEdits = new MultiFileEditProvider();
 
 		for (String fileName : config.getDocumentProvider().getFileNames()) {
 			Node rootNode = config.getDocumentProvider().getRootNode(fileName);
 			for (RequireAndLoadWrapper requireCallNode : NodeProvider.getLoadAndRequireNodes(rootNode)) {
 				if(isRequireFor(config.getOriginalName(), requireCallNode.getFile())) {
					ByteList value = requireCallNode.getRequireStrNode().getValue();
					value.replace(createNewName(requireCallNode.getFile()));
					value.invalidate();
 					fileEdits.addEditProvider(new FileEditProvider(fileName, new SimpleNodeEditProvider(requireCallNode.getRequireStrNode())));
 				}
 			}
 		}
 		
 		return fileEdits.getFileEditProviders();
 	}
 
 	private byte[] createNewName(String requireString) {
 		return requireString.replace(NameHelper.withoutRubyFileExtension(config.getOriginalName()), NameHelper.withoutRubyFileExtension(config.getNewName())).getBytes();
 	}
 
 	private boolean isRequireFor(String originalName, String requireString) {
 		originalName  = NameHelper.withoutRubyFileExtension(originalName);
 		requireString = NameHelper.withoutRubyFileExtension(requireString);
 			
 		return requireString.equals(originalName) || requireString.endsWith(originalName);
 	}
 }
