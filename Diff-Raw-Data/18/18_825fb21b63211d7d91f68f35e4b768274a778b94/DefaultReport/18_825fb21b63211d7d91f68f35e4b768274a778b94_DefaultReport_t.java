 /*******************************************************************************
 * Copyright 2012 Ivan Shubin http://mindengine.net
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
 package net.mindengine.oculus.experior.framework.report;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import net.mindengine.oculus.experior.framework.test.OculusTest;
 import net.mindengine.oculus.experior.reporter.MessageBuilder;
 import net.mindengine.oculus.experior.reporter.MessageContainer;
 import net.mindengine.oculus.experior.reporter.Report;
 import net.mindengine.oculus.experior.reporter.ReportBranchConfiguration;
 import net.mindengine.oculus.experior.reporter.ReportConfiguration;
 import net.mindengine.oculus.experior.reporter.ReportDesign;
 import net.mindengine.oculus.experior.reporter.ReportIcon;
 import net.mindengine.oculus.experior.reporter.nodes.BranchReportNode;
 import net.mindengine.oculus.experior.reporter.nodes.ExceptionReportNode;
 import net.mindengine.oculus.experior.reporter.nodes.ReportNode;
 import net.mindengine.oculus.experior.reporter.nodes.TextReportNode;
 import net.mindengine.oculus.experior.utils.ExceptionUtils;
 
 import org.apache.commons.lang3.StringUtils;
 
 /**
  * This is a simple implementation of the report. It is used in
  * {@link OculusTest}
  * 
  * @author Ivan Shubin
  * 
  */
 
 public class DefaultReport implements Report {
 
     protected static final String ROOT = "root";
     
     /**
      * Used for generating ids for all report nodes
      */
     private transient Long uniqueCount = 0L;
     private BranchReportNode mainBranch;
     private BranchReportNode caret;
     private ReportConfiguration reportConfiguration;
     private MessageContainer messageContainer;
     
     public DefaultReport (ReportConfiguration reportConfiguration) {
         setReportConfiguration(reportConfiguration);
         messageContainer = reportConfiguration.getMessageContainer();
         init();
     }
     
     private void init() {
         mainBranch = new BranchReportNode(ROOT);
         mainBranch.setReport(this);
         caret = mainBranch;
     }
 
     private synchronized String getUniqueId(){
         uniqueCount++;
         return uniqueCount.toString();
     }
     
     @Override
     public BranchReportNode branch(String branch) {
         return branch(branch, null);
     }
     
     @Override
     public BranchReportNode branch(String branch, String title) {
     	ReportBranchConfiguration reportBranchConfiguration = getReportConfiguration().getBranches().get(branch);
         BranchReportNode suitableBranch = findSuitableBranch(reportBranchConfiguration, caret);
         BranchReportNode newBranchNode = new BranchReportNode(branch).id(getUniqueId()).icon(ReportIcon.COMPONENT).title(title);
         suitableBranch.addNode(newBranchNode);
         caret = newBranchNode;
         newBranchNode.setReport(this);
         
         if ( title !=null ) {
         	outputInfo("- " + title);
         }
         else outputInfo("- " + branch);
         return newBranchNode;
     }
 
     @Override
     public void closeBranchById(String id) {
         ReportNode node = findNodeById(id, caret);
         if(node != null && node.getParentBranch() != null) {
             caret = node.getParentBranch();
         }
     }
     
     @Override
     public void closeBranch(BranchReportNode branch) {
         if(branch.getParentBranch() != null) {
             caret = branch.getParentBranch();
         }
         else {
             caret = branch; 
         }
     }
 
     private ReportNode findNodeById(String id, BranchReportNode startFromNode) {
         if(StringUtils.equals(id, startFromNode.getId())) {
             return startFromNode;
         }
         else {
             if(startFromNode.getParentBranch() != null) {
                 return findNodeById(id, startFromNode.getParentBranch());
             }
         }
         return null;
     }
 
     @Override
     public TextReportNode info(String title) {
         TextReportNode textNode = new TextReportNode().title(title).level(ReportNode.INFO).icon(ReportIcon.INFO);
         caret.addNode(textNode);
         textNode.setReport(this);
         
         outputInfo(title);
         return textNode;
     }
 
     @Override
     public TextReportNode warn(String title) {
         TextReportNode textNode = new TextReportNode().title(title).level(ReportNode.WARN).icon(ReportIcon.INFO);
         caret.addNode(textNode);
         textNode.setReport(this);
         
         outputInfo(title);
         return textNode;
     }
 
     @Override
     public TextReportNode error(String title) {
         TextReportNode textNode = new TextReportNode().title(title).level(ReportNode.ERROR).icon(ReportIcon.INFO);
         caret.addNode(textNode);
         textNode.setReport(this);
         
         outputError(title);
         return textNode;
     }
 
     @Override
     public ExceptionReportNode error(Throwable exception) {
        ExceptionReportNode node = new ExceptionReportNode().level(ReportNode.ERROR).exception(exception).icon(ReportIcon.EXCEPTION);
         node.setReport(this);
         caret.addNode(node);
         
         outputError(exception);
         return node;
     }
     
 
 	@Override
     public <T extends ReportNode> T addnode(T node) {
         caret.addNode(node);
         return node;
     }
 
     @Override
     public BranchReportNode getMainBranch() {
         return mainBranch;
     }
     
     @Override
     public MessageBuilder message(String messageName) {
     	return message(messageName, "Uknown message '" + messageName + "'");
     }
     
     @Override
     public MessageBuilder message(String messageName, String defaultValue) {
     	if( getMessageContainer() != null ) {
     		return getMessageContainer().message(messageName, defaultValue);
     	}
     	else return new MessageBuilder(defaultValue);
     }
     
     private void outputInfo(String text) {
     	try {
     		if ( getReportConfiguration().getOutputStreamOut() != null ) {
     			output( getReportConfiguration().getOutputStreamOut(), text);
     		}
     	}
     	catch (Exception e) {
 		}
 	}
 
 	private void outputError(String text) {
     	try {
     		if ( getReportConfiguration().getOutputStreamErr() != null ) {
     			output( getReportConfiguration().getOutputStreamErr(), text);
     		}
     	}
     	catch (Exception e) {
 		}
 	}
 
 	private void output(OutputStream stream, String text) throws IOException {
 		printIndentation(stream);
 		if( text !=null ) {
 			stream.write((ReportDesign.removeDecorationTags(text) + "\n").getBytes());
 		}
 	}
 	
     private void printIndentation(OutputStream stream) throws IOException {
 		if( getReportConfiguration().getOutputIndentation() > 0) {
 			String indentationBlock = StringUtils.repeat(" ", getReportConfiguration().getOutputIndentation());
 			printIndentation(stream, indentationBlock, caret);
 		}
 	}
 
 	private void printIndentation(OutputStream stream, String indentationBlock, BranchReportNode node) throws IOException {
 		if( node.getParentBranch() != null ) {
 			stream.write(indentationBlock.getBytes());
 			printIndentation(stream, indentationBlock, node.getParentBranch());
 		}
 	}
 
 	private void outputError(Throwable exception) {
     	try {
     		if ( getReportConfiguration().getOutputStreamErr() != null ) {
     			printStackTraceToStream(exception, getReportConfiguration().getOutputStreamErr());
     		}
     	}
     	catch (Exception e) {
 		}
     }
     
 	private void printStackTraceToStream(Throwable exception, OutputStream stream) throws IOException {
 		stream.write(ExceptionUtils.convertExceptionToString(exception).getBytes());
 		stream.write("\n".getBytes());
 		
 	}
 
 	private BranchReportNode findSuitableBranch(ReportBranchConfiguration reportBranchConfiguration, BranchReportNode fromBranchNode) {
         if(reportBranchConfiguration != null) {
             if(reportBranchConfiguration.allowsParent(fromBranchNode.getBranch())){
                 return fromBranchNode;
             }
             else {
                 if(fromBranchNode.getParentBranch() != null) {
                     return findSuitableBranch(reportBranchConfiguration, fromBranchNode.getParentBranch());
                 }
             }
         }
         return fromBranchNode;
     }
 
 
 	public ReportConfiguration getReportConfiguration() {
 		return reportConfiguration;
 	}
 
 	public void setReportConfiguration(ReportConfiguration reportConfiguration) {
 		this.reportConfiguration = reportConfiguration;
 	}
 
 	public MessageContainer getMessageContainer() {
 		return messageContainer;
 	}
 
 	public void setMessageContainer(MessageContainer messageContainer) {
 		this.messageContainer = messageContainer;
 	}
 }
