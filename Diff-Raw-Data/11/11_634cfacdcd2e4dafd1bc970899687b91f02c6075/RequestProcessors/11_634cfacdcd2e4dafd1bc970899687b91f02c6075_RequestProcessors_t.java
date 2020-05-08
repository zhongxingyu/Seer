 /*
  * ====================================================================
  *
  * Frame2 Open Source License
  *
  * Copyright (c) 2004-2007 Megatome Technologies.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. The end-user documentation included with the redistribution, if
  *    any, must include the following acknowlegement:
  *       "This product includes software developed by
  *        Megatome Technologies."
  *    Alternately, this acknowlegement may appear in the software itself,
  *    if and wherever such third-party acknowlegements normally appear.
  *
  * 4. The names "The Frame2 Project", and "Frame2", 
  *    must not be used to endorse or promote products derived
  *    from this software without prior written permission. For written
  *    permission, please contact iamthechad@sourceforge.net.
  *
  * 5. Products derived from this software may not be called "Frame2"
  *    nor may "Frame2" appear in their names without prior written
  *    permission of Megatome Technologies.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL MEGATOME TECHNOLOGIES OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  */
 package org.megatome.frame2.model;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import org.megatome.frame2.Frame2Plugin;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class RequestProcessors extends Frame2DomainObject {
 
 	private HttpRequestProcessor httpRequestProcessor;
 
 	private SoapRequestProcessor soapRequestProcessor;
 
 	public RequestProcessors() {
 		clearComments();
 	}
 	
 	@Override
 	public RequestProcessors copy() {
 		return new RequestProcessors(this);
 	}
 
 	// Deep copy
 	private RequestProcessors(final RequestProcessors source) {
		if (source.httpRequestProcessor != null) {
			this.httpRequestProcessor = source.httpRequestProcessor.copy();
		}
		if (source.soapRequestProcessor != null) {
			this.soapRequestProcessor = source.soapRequestProcessor.copy();
		}
 		setComments(source.getCommentMap());
 	}
	
 	// This attribute is optional
 	public void setHttpRequestProcessor(final HttpRequestProcessor value) {
 		this.httpRequestProcessor = value;
 	}
 
 	public HttpRequestProcessor getHttpRequestProcessor() {
 		return this.httpRequestProcessor;
 	}
 
 	// This attribute is optional
 	public void setSoapRequestProcessor(final SoapRequestProcessor value) {
 		this.soapRequestProcessor = value;
 	}
 
 	public SoapRequestProcessor getSoapRequestProcessor() {
 		return this.soapRequestProcessor;
 	}
 
 	public void writeNode(final Writer out, final String nodeName,
 			final String indent) throws IOException {
 		out.write(indent);
 		out.write(Frame2Plugin.getResourceString("Frame2Model.tagStart")); //$NON-NLS-1$
 		out.write(nodeName);
 		out.write(Frame2Plugin.getResourceString("Frame2Model.tagFinish")); //$NON-NLS-1$
 		final String nextIndent = indent
 				+ Frame2Plugin.getResourceString("Frame2Model.indentTabValue"); //$NON-NLS-1$
 		if (this.httpRequestProcessor != null) {
 			this.httpRequestProcessor.writeNode(out, Frame2Plugin
 					.getResourceString("Frame2Model.http-request-processor"), //$NON-NLS-1$
 					nextIndent);
 		}
 		if (this.soapRequestProcessor != null) {
 			this.soapRequestProcessor.writeNode(out, Frame2Plugin
 					.getResourceString("Frame2Model.soap-request-processor"), //$NON-NLS-1$
 					nextIndent);
 		}
 
 		writeRemainingComments(out, indent);
 		out.write(indent);
 		out
 				.write(Frame2Plugin
 						.getResourceString("Frame2Model.endTagStart") + nodeName + Frame2Plugin.getResourceString("Frame2Model.tagFinish")); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	public void readNode(final Node node) {
 		final NodeList children = node.getChildNodes();
 		for (int i = 0, size = children.getLength(); i < size; ++i) {
 			final Node childNode = children.item(i);
 			final String childNodeName = (childNode.getLocalName() == null ? childNode
 					.getNodeName().intern()
 					: childNode.getLocalName().intern());
 			if (childNodeName.equals(Frame2Plugin
 					.getResourceString("Frame2Model.http-request-processor"))) { //$NON-NLS-1$
 				this.httpRequestProcessor = new HttpRequestProcessor();
 				this.httpRequestProcessor.readNode(childNode);
 			} else if (childNodeName.equals(Frame2Plugin
 					.getResourceString("Frame2Model.soap-request-processor"))) { //$NON-NLS-1$
 				this.soapRequestProcessor = new SoapRequestProcessor();
 				this.soapRequestProcessor.readNode(childNode);
 			} else {
 				// Found extra unrecognized childNode
 				if (childNodeName.equals(Frame2Plugin
 						.getResourceString("Frame2Model.comment"))) { //$NON-NLS-1$
 					recordComment(childNode, i);
 				}
 			}
 		}
 	}
 
 	public void validate() throws Frame2Config.ValidateException {
 		// Validating property httpRequestProcessor
 		if (getHttpRequestProcessor() != null) {
 			getHttpRequestProcessor().validate();
 		}
 		// Validating property soapRequestProcessor
 		if (getSoapRequestProcessor() != null) {
 			getSoapRequestProcessor().validate();
 		}
 	}
 
 	@Override
 	public boolean equals(final Object o) {
 		if (o == this) {
 			return true;
 		}
 		if (!(o instanceof RequestProcessors)) {
 			return false;
 		}
 		final RequestProcessors inst = (RequestProcessors) o;
 		if (!(this.httpRequestProcessor == null ? inst.httpRequestProcessor == null
 				: this.httpRequestProcessor.equals(inst.httpRequestProcessor))) {
 			return false;
 		}
 		if (!(this.soapRequestProcessor == null ? inst.soapRequestProcessor == null
 				: this.soapRequestProcessor.equals(inst.soapRequestProcessor))) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		int result = 17;
 		result = 37
 				* result
 				+ (this.httpRequestProcessor == null ? 0
 						: this.httpRequestProcessor.hashCode());
 		result = 37
 				* result
 				+ (this.soapRequestProcessor == null ? 0
 						: this.soapRequestProcessor.hashCode());
 		return result;
 	}
 
 }
