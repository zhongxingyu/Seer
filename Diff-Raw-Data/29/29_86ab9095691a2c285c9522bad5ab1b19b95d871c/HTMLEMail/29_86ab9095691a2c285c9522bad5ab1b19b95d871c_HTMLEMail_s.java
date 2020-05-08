 package com.openntf.xsnippets;
 /**
  * Copied from XSnippets
  * http://openntf.org/XSnippets.nsf/snippet.xsp?id=emailbean-send-dominodocument-html-emails-cw-embedded-images-attachments-custom-headerfooter
  */
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.faces.context.FacesContext;
 
 import lotus.domino.Database;
 import lotus.domino.Document;
 import lotus.domino.EmbeddedObject;
 import lotus.domino.MIMEEntity;
 import lotus.domino.MIMEHeader;
 import lotus.domino.NotesException;
 import lotus.domino.Session;
 import lotus.domino.Stream;
 
 import com.ibm.commons.util.NotImplementedException;
 import com.ibm.domino.xsp.module.nsf.NotesContext;
 import com.ibm.xsp.model.FileRowData;
 import com.ibm.xsp.model.domino.wrapped.DominoDocument;
 import com.ibm.xsp.model.domino.wrapped.DominoRichTextItem;
 import com.ibm.xsp.model.domino.wrapped.DominoDocument.AttachmentValueHolder;
 import com.ibm.xsp.persistence.PersistedContent;
 
 public class HTMLEMail implements Serializable {

	/**
	 * 
	 */
 	private static final long serialVersionUID = 1L;
 	private ArrayList<String> sendTo;
 	private ArrayList<String> ccList;
 	private ArrayList<String> bccList;
 	// UK: 25.04.2012 added contentList
 	private StringBuilder contentList;
 	private String senderEmail;
 	private String senderName;
 	private String subject;
 
 	private DominoDocument document;
 	private String fieldName;
 	// UK: 25.04.2012 modified, set default value to an empty string
 	// otherwise, if bannerHTML and footerHTML are not set, NULL is added to the
 	// mail document
 	private String bannerHTML = "";
 	private String footerHTML = "";
 
 	private boolean debugMode = false;
 
 	private static final Pattern imgRegExp = Pattern
 			.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
 
 	// UK: 25.04.2012 added. The bean can be called from within another java
 	// class then
 	public static final String BEAN_NAME = "htmlemail"; // name of the bean
 
 	public static HTMLEMail get() {
 		FacesContext context = FacesContext.getCurrentInstance();
 		HTMLEMail bean = (HTMLEMail) context.getApplication().getVariableResolver()
 				.resolveVariable(context, BEAN_NAME);
 		return bean;
 	}
 
 	// -------------------------------------------------------------------------
 
 	public HTMLEMail() {
 		this.subject = "";
 		this.sendTo = new ArrayList<String>();
 		this.ccList = new ArrayList<String>();
 		this.bccList = new ArrayList<String>();
 		// UK: 25.04.2012 added
 		this.contentList = new StringBuilder();
 	}
 
 	// -------------------------------------------------------------------------
 
 	public String getSendTo() {
 		if (this.isDebugMode()) {
 			System.out.println("getSendTo() : " + this.sendTo.toString());
 		}
 		return this.sendTo.toString().replace("[", "").replace("]", "");
 	}
 
 	public void setSendTo(final String sendTo) {
 		this.sendTo.add(sendTo);
 	}
 
 	// -------------------------------------------------------------------------
 
 	public String getCcList() {
 		if (this.isDebugMode()) {
 			System.out.println("getCcList() : " + this.ccList.toString());
 		}
 		return this.ccList.toString().replace("[", "").replace("]", "");
 	}
 
 	public void setCcList(final String ccList) {
 		this.ccList.add(ccList);
 	}
 
 	// -------------------------------------------------------------------------
 
 	public String getBccList() {
 		if (this.isDebugMode()) {
 			System.out.println("getBccList() : " + this.bccList.toString());
 		}
 		return this.bccList.toString().replace("[", "").replace("]", "");
 	}
 
 	public void setBccList(final String bccList) {
 		this.bccList.add(bccList);
 	}
 
 	// -------------------------------------------------------------------------
 
 	public String getSenderEmail() {
 		return this.senderEmail;
 	}
 
 	public void setSenderEmail(final String senderEmail) {
 		this.senderEmail = senderEmail;
 	}
 
 	// -------------------------------------------------------------------------
 
 	public String getSenderName() {
 		return this.senderName;
 	}
 
 	public void setSenderName(final String senderName) {
 		this.senderName = senderName;
 	}
 
 	// -------------------------------------------------------------------------
 
 	public String getSubject() {
 		return this.subject;
 	}
 
 	public void setSubject(final String subject) {
 		this.subject = subject;
 	}
 
 	// -------------------------------------------------------------------------
 
 	public boolean isDebugMode() {
 		return this.debugMode;
 	}
 
 	public void setDebugMode(final boolean debugMode) {
 		this.debugMode = debugMode;
 	}
 
 	// -------------------------------------------------------------------------
 
 	private Session getCurrentSession() {
 		NotesContext nc = NotesContext.getCurrentUnchecked();
 		return (null != nc) ? nc.getCurrentSession() : null;
 	}
 
 	// -------------------------------------------------------------------------
 
 	private Database getCurrentDatabase() {
 		NotesContext nc = NotesContext.getCurrentUnchecked();
 		return (null != nc) ? nc.getCurrentDatabase() : null;
 	}
 
 	// -------------------------------------------------------------------------
 
 	public void send() throws NotesException, IOException, Exception {
 		Session session = getCurrentSession();
 		Database database = getCurrentDatabase();
 		if (this.isDebugMode()) {
 			System.out.println("Started send()");
 		}
 		if (null != session && null != database && null != this.sendTo
 				&& null != this.subject && null != this.senderEmail) {
 			try {
 
 				session.setConvertMime(false);
 				Document emailDocument = database.createDocument();
 
 				MIMEEntity emailRoot = emailDocument.createMIMEEntity("Body");
 
 				if (null != emailRoot) {
 					MIMEHeader emailHeader = emailRoot.createHeader("Reply-To");
 					emailHeader.setHeaderVal(this.getSenderEmail());
 
 					emailHeader = emailRoot.createHeader("Return-Path");
 					emailHeader.setHeaderVal(this.getSenderEmail());
 
 					final String fromSender = (null == this.getSenderName()) ? this
 							.getSenderEmail()
 							: "\"" + this.getSenderName() + "\" <"
 									+ this.getSenderEmail() + ">";
 
 					emailHeader = emailRoot.createHeader("From");
 					emailHeader.setHeaderVal(fromSender);
 
 					emailHeader = emailRoot.createHeader("Sender");
 					emailHeader.setHeaderVal(fromSender);
 
 					emailHeader = emailRoot.createHeader("To");
 					emailHeader.setHeaderVal(this.getSendTo());
 
 					if (!this.ccList.isEmpty()) {
 						emailHeader = emailRoot.createHeader("CC");
 						emailHeader.setHeaderVal(this.getCcList());
 					}
 
 					if (!this.bccList.isEmpty()) {
 						emailHeader = emailRoot.createHeader("BCC");
 						emailHeader.setHeaderVal(this.getBccList());
 					}
 
 					emailHeader = emailRoot.createHeader("Subject");
 					emailHeader.setHeaderVal(this.getSubject());
 
 					MIMEEntity emailRootChild = emailRoot.createChildEntity();
 					if (null != emailRootChild) { // UK: 25.04.2012 modified
 						String boundary = System.currentTimeMillis() + "- CV2"
 								+ System.currentTimeMillis();
 						if (null != this.document) {
 							boundary = System.currentTimeMillis() + "-"
 									+ this.document.getDocumentId();
 						}
 						emailHeader = emailRootChild
 								.createHeader("Content-Type");
 						emailHeader
 								.setHeaderVal("multipart/alternative; boundary=\""
 										+ boundary + "\"");
 
 						MIMEEntity emailChild = emailRootChild
 								.createChildEntity();
 						if (null != emailChild) {
 							String contentAsText = "";
 							if (null != this.document) {
 								contentAsText = this.document.getRichTextItem(
 										this.fieldName).getContentAsText();
 							}
 							Stream stream = session.createStream();
 							stream.writeText(contentAsText);
 							emailChild.setContentFromText(stream,
 									"text/plain; charset=\"UTF-8\"",
 									MIMEEntity.ENC_NONE);
 							stream.close();
 
 							emailChild = emailRootChild.createChildEntity();
 							stream = session.createStream();
 							stream.writeText(this.getHTML());
 							emailChild.setContentFromText(stream,
 									"text/html; charset=\"UTF-8\"",
 									MIMEEntity.ENC_NONE);
 							stream.close();
 							stream.recycle();
 							stream = null;
 						}
 
 						// add embedded images....
 						final List<FileRowData> embeddedImages = this
 								.getEmbeddedImagesList();
 						if (null != embeddedImages && !embeddedImages.isEmpty()) {
 							if (this.isDebugMode()) {
 								System.out.println("Adding Embedded Images...");
 							}
 							for (FileRowData embeddedImage : embeddedImages) {
 								emailRootChild = emailRoot.createChildEntity();
 								if (null != emailRootChild
 										&& embeddedImage instanceof AttachmentValueHolder) {
 									InputStream is = null;
 									try {
 										String persistentName = ((AttachmentValueHolder) embeddedImage)
 												.getPersistentName();
 										String cid = ((AttachmentValueHolder) embeddedImage)
 												.getCID();
 										emailHeader = emailRootChild
 												.createHeader("Content-Disposition");
 										emailHeader
 												.setHeaderVal("inline; filename=\""
 														+ persistentName + "\"");
 										emailHeader = emailRootChild
 												.createHeader("Content-ID");
 										emailHeader.setHeaderVal("<" + cid
 												+ ">");
 										is = this
 												.getEmbeddedImageStream(persistentName);
 										Stream stream = session.createStream();
 										stream.setContents(is);
 										emailRootChild.setContentFromBytes(
 												stream,
 												embeddedImage.getType(),
 												MIMEEntity.ENC_IDENTITY_BINARY);
 										if (this.isDebugMode()) {
 											System.out
 													.println("Added Embedded Image : "
 															+ persistentName);
 										}
 									} catch (IOException e) {
 										if (this.isDebugMode()) {
 											System.out
 													.println("Adding Embedded Image failed : "
 															+ e.getMessage());
 										}
 										throw e;
 									} finally {
 										if (null != is) {
 											is.close();
 											is = null;
 										}
 									}
 								}
 							}
 							if (this.isDebugMode()) {
 								System.out
 										.println("Completed Adding Embedded Images");
 							}
 						}
 
 						if (null != this.document) { // UK: 25.04.2012 added
 						// add attachments....
 							final List<FileRowData> attachments = this
 									.getDocument().getAttachmentList(
 											this.getFieldName());
 							if (null != attachments && !attachments.isEmpty()) {
 								if (this.isDebugMode()) {
 									System.out.println("Adding Attachments...");
 								}
 								for (FileRowData attachment : attachments) {
 									emailRootChild = emailRoot
 											.createChildEntity();
 									if (null != emailRootChild
 											&& attachment instanceof AttachmentValueHolder) {
 										InputStream is = null;
 										try {
 											String persistentName = ((AttachmentValueHolder) attachment)
 													.getPersistentName();
 											String cid = ((AttachmentValueHolder) attachment)
 													.getCID();
 											EmbeddedObject eo = this
 													.getDocument()
 													.getDocument()
 													.getAttachment(
 															persistentName);
 											if (null != eo) {
 												emailHeader = emailRootChild
 														.createHeader("Content-Disposition");
 												emailHeader
 														.setHeaderVal("attachment; filename=\""
 																+ persistentName
 																+ "\"");
 												emailHeader = emailRootChild
 														.createHeader("Content-ID");
 												emailHeader.setHeaderVal("<"
 														+ cid + ">");
 												is = eo.getInputStream();
 												Stream stream = session
 														.createStream();
 												stream.setContents(is);
 												emailRootChild
 														.setContentFromBytes(
 																stream,
 																attachment
 																		.getType(),
 																MIMEEntity.ENC_IDENTITY_BINARY);
 												if (this.isDebugMode()) {
 													System.out
 															.println("Added Attachment : "
 																	+ persistentName);
 												}
 											}
 										} catch (Exception e) {
 											if (this.isDebugMode()) {
 												System.out
 														.println("Adding Attachment failed : "
 																+ e
 																		.getMessage());
 											}
 											throw e;
 										} finally {
 											if (null != is) {
 												is.close();
 												is = null;
 											}
 										}
 									}
 								}
 								if (this.isDebugMode()) {
 									System.out
 											.println("Completed Adding Attachments");
 								}
 							}
 						}
 					}
 				}
 				try{
 					emailDocument.send();
 				}catch(Exception e){
 					
 				}
 				session.setConvertMime(true);
 				if (this.isDebugMode()) {
 					System.out.println("Completed send()");
 				}
 			} catch (NotesException e) {
 				if (this.isDebugMode()) {
 					System.out.println("Failed send() with NotesException"
 							+ e.getMessage());
 				}
 				throw e;
 			} catch (IOException e) {
 				if (this.isDebugMode()) {
 					System.out.println("Failed send() with IOException"
 							+ e.getMessage());
 				}
 				throw e;
 			} catch (Exception e) {
 				if (this.isDebugMode()) {
 					System.out.println("Failed send() with Exception"
 							+ e.getMessage());
 				}
 				throw e;
 			}
 		}
 	}
 
 	// -------------------------------------------------------------------------
 
 	public DominoDocument getDocument() {
 		return this.document;
 	}
 
 	public void setDocument(final DominoDocument document) {
 		this.document = document;
 	}
 
 	// -------------------------------------------------------------------------
 
 	public String getFieldName() {
 		return this.fieldName;
 	}
 
 	public void setFieldName(final String fieldName) {
 		this.fieldName = fieldName;
 	}
 
 	// -------------------------------------------------------------------------
 
 	public List<FileRowData> getEmbeddedImagesList() throws NotesException {
 		if (null != document && null != fieldName) {
 			return document.getEmbeddedImagesList(fieldName);
 		}
 		return null;
 	}
 
 	// -------------------------------------------------------------------------
 
 	private InputStream getEmbeddedImageStream(final String fileName)
 			throws NotesException, IOException {
 		if (null != document && null != fieldName && null != fileName) {
 			final DominoRichTextItem drti = document.getRichTextItem(fieldName);
 			if (null != drti) {
 				final PersistedContent pc = drti.getPersistedContent(
 						FacesContext.getCurrentInstance(), fieldName, fileName);
 				if (null != pc) {
 					return pc.getInputStream();
 				}
 			}
 		}
 		return null;
 	}
 
 	// -------------------------------------------------------------------------
 
 	public String getHTML() {
 		StringBuffer html = new StringBuffer();
 		html.append(getBannerHTML());
 		// UK: 25.04.2012 modified
 		if (null != getBodyHTML()) {
 			html.append(getBodyHTML());
 		}
 		// UK: 25.04.2012 added
 		html.append(getContentHTML());
 		html.append(getFooterHTML());
 		return html.toString();
 	}
 
 	// -------------------------------------------------------------------------
 
 	public String getBannerHTML() {
 		return this.bannerHTML;
 	}
 
 	public void setBannerHTML(final String bannerHTML) {
 		this.bannerHTML = bannerHTML;
 	}
 
 	// -------------------------------------------------------------------------
 
 	// UK: 25.04.2012 modified
 	public String getContentHTML() {
 		if (this.isDebugMode()) {
 			System.out.println("Started getContentHTML()");
 			System.out.println(this.contentList.toString());
 		}
 		return this.contentList.toString();
 	}
 
 	// UK: 25.04.2012 modified
 	public void setContentHTML(final String contentHTML) {
 		this.contentList.append(contentHTML);
 	}
 
 	// -------------------------------------------------------------------------
 	// UK: 25.04.2012 added
 	public void addHTML(final String contentHTML) {
 		this.contentList.append(contentHTML);
 	}
 
 	// -------------------------------------------------------------------------
 
 	public String getBodyHTML() {
 		if (null != document && null != fieldName) {
 			if (this.isDebugMode()) {
 				System.out.println("Started getBodyHTML()");
 			}
 			final DominoRichTextItem drti = document.getRichTextItem(fieldName);
 			if (null != drti) {
 				try {
 					String html = drti.getHTML();
 					if (null != html) {
 						final List<FileRowData> fileRowDataList = document
 								.getEmbeddedImagesList(fieldName);
 						if (null != fileRowDataList) {
 							final Matcher matcher = imgRegExp.matcher(html);
 							while (matcher.find()) {
 								String src = matcher.group();
 								final String srcToken = "src=\"";
 								final int x = src.indexOf(srcToken);
 								final int y = src.indexOf("\"", x
 										+ srcToken.length());
 								final String srcText = src.substring(x
 										+ srcToken.length(), y);
 								for (FileRowData fileRowData : fileRowDataList) {
 									final String srcImage = fileRowData
 											.getHref();
 									final String cidImage = ((AttachmentValueHolder) fileRowData)
 											.getCID();
 									if (srcText.endsWith(srcImage)) {
 										final String newSrc = src.replace(
 												srcText, "cid:" + cidImage);
 										html = html.replace(src, newSrc);
 										if (this.isDebugMode()) {
 											System.out
 													.println("CID referenced image: "
 															+ srcText
 															+ " with CID:"
 															+ cidImage);
 										}
 									}
 								}
 							}
 						}
 					}
 					if (this.isDebugMode()) {
 						System.out.println("Completed getBodyHTML() : " + html);
 					}
 					return html;
 				} catch (Exception e) {
 					if (this.isDebugMode()) {
 						System.out.println("Failed getBodyHTML() : "
 								+ e.getMessage());
 					}
 				}
 			}
 		}
 		return "";
 	}
 
 	@Deprecated
 	public void setBodyHTML(final String bodyHTML)
 			throws NotImplementedException {
 		if (this.isDebugMode()) {
 			System.out.println("Method setBodyHTML(string) is not permitted");
 		}
 		throw new NotImplementedException();
 	}
 
 	// -------------------------------------------------------------------------
 
 	public String getFooterHTML() {
 		return this.footerHTML;
 	}
 
 	public void setFooterHTML(final String footerHTML) {
 		this.footerHTML = footerHTML;
 	}
 
 	// -------------------------------------------------------------------------
 
 } // end EmailBean
