 /*
  * The MIT License
  *
  * Copyright (c) <2012> <Bruno P. Kinoshita>
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.biouno.structure;
 
 import hudson.CopyOnWrite;
 import hudson.model.Descriptor;
 import hudson.tasks.Builder;
 import hudson.util.FormValidation;
 import net.sf.json.JSONObject;
 
 import org.apache.commons.lang.StringUtils;
 import org.biouno.structure.util.Messages;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Descriptor of Structure builder.
  * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
  * @since 0.1
  * @see {@link StructureBuilder}
  */
 public class StructureBuilderDescriptor extends Descriptor<Builder> {
 	/**
 	 * Exposed for jelly.
 	 */
 	public final Class<StructureBuilder> builderType = StructureBuilder.class;
 	/**
 	 * Structure name displayed in the build configuration screen.
 	 */
 	private static final String DISPLAY_NAME = Messages.StructureDescriptor_DisplayName();
 	/**
 	 * The list of available installations. They are copied when the form is 
 	 * submitted.
 	 */
 	@CopyOnWrite
 	private volatile StructureInstallation[] installations = new StructureInstallation[0];
 	/**
 	 * No args constructor to ensure the descriptor pattern.
 	 */
 	public StructureBuilderDescriptor() {
 		super(StructureBuilder.class);
 		load();
 	}
 	/* (non-Javadoc)
 	 * @see hudson.model.Descriptor#getDisplayName()
 	 */
 	@Override
 	public String getDisplayName() {
 		return DISPLAY_NAME;
 	}
 	/**
 	 * Gets the list of installations. Never <code>null</code>.
 	 * @return StructureInstallation[]
 	 */
 	public StructureInstallation[] getInstallations() {
 		return this.installations;
 	}
 	/**
 	 * Gets an installation by its name, or <code>null</code> if none found.
 	 * @param name the installation name
 	 * @return StructureInstallation or <code>null</code>
 	 */
 	public StructureInstallation getInstallationByName(String name) {
 		StructureInstallation found = null;
 		for(StructureInstallation installation : this.installations) {
 			if (StringUtils.isNotEmpty(installation.getName())) {
 				if(name.equals(installation.getName())) {
 					found = installation;
 					break;
 				}
 			}
 		}
 		return found;
 	}
 	/* (non-Javadoc)
 	 * @see hudson.model.Descriptor#configure(org.kohsuke.stapler.StaplerRequest, net.sf.json.JSONObject)
 	 */
 	@Override
 	public boolean configure(StaplerRequest req, JSONObject json)
 			throws hudson.model.Descriptor.FormException {
 		this.installations = req.bindParametersToList(StructureInstallation.class, "Structure.").toArray(new StructureInstallation[0]);
 		save();
 		return Boolean.TRUE;
 	}
 	@Override
 	public Builder newInstance(StaplerRequest arg0, JSONObject arg1)
 			throws hudson.model.Descriptor.FormException {
 		return super.newInstance(arg0, arg1);
 	}
 	/**
 	 * Validates required fields.
 	 * @param value the value
 	 * @return FormValidation
 	 */
 	public FormValidation doRequired(@QueryParameter String value) {
 		FormValidation returnValue = FormValidation.ok();
 		if(StringUtils.isBlank(value)) {
 			returnValue = FormValidation.error(Messages.StructureDescriptor_Required());
 		}
 		return returnValue;
 	}
 	/**
 	 * Validates required long fields.
 	 * @param value the value
 	 * @return FormValidation
 	 */
 	public FormValidation doLongRequired(@QueryParameter String value) {
 		FormValidation returnValue = FormValidation.ok();
 		if(StringUtils.isNotBlank(value)) {
 			try {
 				Long.parseLong(value);
 			} catch ( NumberFormatException nfe ) {
 				returnValue = FormValidation.error("This value must be an integer");
 			}
 		}
 		return returnValue;
 	}
 	/**
 	 * Validates required double fields.
 	 * @param value the value
 	 * @return FormValidation
 	 */
 	public FormValidation doDoubleRequired(@QueryParameter String value) {
 		FormValidation returnValue = FormValidation.ok();
 		if(StringUtils.isNotBlank(value)) {
 			try {
 				Double.parseDouble(value);
 			} catch ( NumberFormatException nfe ) {
 				returnValue = FormValidation.error("This value must be an float");
 			}
 		}
 		return returnValue;
 	}
 }
