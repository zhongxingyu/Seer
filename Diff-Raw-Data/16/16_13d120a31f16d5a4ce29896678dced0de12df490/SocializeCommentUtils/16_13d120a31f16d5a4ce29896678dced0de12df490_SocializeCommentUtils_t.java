 /*
  * Copyright (c) 2012 Socialize Inc.
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
 package com.socialize.api.action.comment;
 
 import android.app.Activity;
 import android.app.Dialog;
 import com.socialize.ShareUtils;
 import com.socialize.UserUtils;
 import com.socialize.api.SocializeSession;
 import com.socialize.api.action.SocializeActionUtilsBase;
 import com.socialize.entity.Comment;
 import com.socialize.entity.Entity;
 import com.socialize.entity.User;
 import com.socialize.error.SocializeException;
 import com.socialize.listener.comment.CommentAddListener;
 import com.socialize.listener.comment.CommentGetListener;
 import com.socialize.listener.comment.CommentListListener;
 import com.socialize.networks.ShareOptions;
 import com.socialize.networks.SocialNetwork;
 import com.socialize.ui.auth.IAuthDialogFactory;
 import com.socialize.ui.auth.AuthDialogListener;
 import com.socialize.ui.auth.AuthPanelView;
 import com.socialize.ui.dialog.SafeProgressDialog;
 import com.socialize.ui.share.DialogFlowController;
 import com.socialize.ui.share.IShareDialogFactory;
 import com.socialize.ui.share.ShareDialogListener;
 import com.socialize.ui.share.SharePanelView;
 
 
 /**
  * @author Jason Polites
  */
 public class SocializeCommentUtils extends SocializeActionUtilsBase implements CommentUtilsProxy {
 	
 	private CommentSystem commentSystem;
 	private IAuthDialogFactory authDialogFactory;
 	private IShareDialogFactory shareDialogFactory;
 
 	@Override
 	public void addComment(Activity context, final Entity entity, final String text, final CommentAddListener listener) {
 		
 		final SocializeSession session = getSocialize().getSession();
 		
 		if(isDisplayAuthDialog()) {
 			authDialogFactory.show(context, new AuthDialogListener() {
 				
 				@Override
 				public void onShow(Dialog dialog, AuthPanelView dialogView) {}
 				
 				@Override
 				public void onCancel(Dialog dialog) {
 					if(listener != null) {
 						listener.onCancel();
 					}
 				}
 				
 				@Override
 				public void onSkipAuth(Activity context, Dialog dialog) {
 					dialog.dismiss();
 					doCommentWithoutShare(context, session, entity, text, listener);
 				}
 
 				@Override
 				public void onError(Activity context, Dialog dialog, Exception error) {
 					dialog.dismiss();
 					if(listener != null) {
 						listener.onError(SocializeException.wrap(error));
 					}
 				}
 
 				@Override
 				public void onAuthenticate(Activity context, Dialog dialog, SocialNetwork network) {
 					dialog.dismiss();
 					doCommentWithShare(context, session, entity, text, listener);
 				}
 			});
 		}
 		else {
 			doCommentWithShare(context, session, entity, text, listener);
 		}		
 	}
 	
 	@Override
 	public void addComment(final Activity context, final Entity entity, final String text, final ShareOptions shareOptions, final CommentAddListener listener) {
 		final SocializeSession session = getSocialize().getSession();
 		
 		if(isDisplayAuthDialog(shareOptions)) {
 			
 			authDialogFactory.show(context, new AuthDialogListener() {
 				
 				@Override
 				public void onShow(Dialog dialog, AuthPanelView dialogView) {}
 				
 				@Override
 				public void onCancel(Dialog dialog) {
 					if(listener != null) {
 						listener.onCancel();
 					}
 				}
 				
 				@Override
 				public void onSkipAuth(Activity context, Dialog dialog) {
 					dialog.dismiss();
 					doCommentWithoutShare(context, session, entity, text, shareOptions, listener);
 				}
 
 				@Override
 				public void onError(Activity context, Dialog dialog, Exception error) {
 					dialog.dismiss();
 					if(listener != null) {
 						listener.onError(SocializeException.wrap(error));
 					}
 				}
 
 				@Override
 				public void onAuthenticate(Activity context, Dialog dialog, SocialNetwork network) {
 					dialog.dismiss();
 					doCommentWithoutShare(context, session, entity, text, shareOptions, listener);
 				}
 			});
 		}
 		else {
 			doCommentWithoutShare(context, session, entity, text, shareOptions, listener);
 		}			
 	}
 
 	protected void doCommentWithShare(final Activity context, final SocializeSession session, final Entity entity, final String text, final CommentAddListener listener) {
 		
 		if(isDisplayShareDialog()) {
 			shareDialogFactory.show(context, entity, null,  new ShareDialogListener() {
 				@Override
 				public void onShow(Dialog dialog, SharePanelView dialogView) {}
 				
 				@Override
 				public void onFlowInterrupted(DialogFlowController controller) {}
 				
 				@Override
 				public boolean onContinue(final Dialog dialog, boolean remember, final SocialNetwork...networks) {
 					
 					int count = 0;
 					
 					User user = session.getUser();
 					
 					if(networks != null) {
 						count = networks.length;
 					}
 					
 					final SafeProgressDialog progress = SafeProgressDialog.show(context, "Posting comment", "Please wait...", count);
 					
 					if(remember && user.setAutoPostPreferences(networks)) {
 						UserUtils.saveUserSettings(context, user, null);
 					}
 
 					ShareOptions options = new ShareOptions();
 					options.setShareLocation(user.isShareLocation());
 					options.setShareTo(networks);
 
 					CommentAddListener overrideListener = new CommentAddListener() {
 
 						@Override
 						public void onError(SocializeException error) {
 							progress.dismissAll();
 							dialog.dismiss();
 							if(listener != null) {
 								listener.onError(error);
 							}
 						}
 
 						@Override
 						public void onCreate(Comment comment) {
 							
 							if(listener != null) {
 								listener.onCreate(comment);
 							}
 							
 							doActionShare(context, comment, text, progress, listener, networks);
 							
 							dialog.dismiss();
 						}
 					};
 
 					commentSystem.addComment(session, entity, text, options, overrideListener);
 
 					return false;				
 				}
 				@Override
 				public void onCancel(Dialog dialog) {
 					if(listener != null) {
 						listener.onCancel();
 					}
 				}
 			}, ShareUtils.COMMENT_AND_LIKE|ShareUtils.SHOW_REMEMBER);
 		}
 		else {
 			doCommentWithoutShare(context, session, entity, text, listener);
 		}
 	}	
 	
 	protected void doCommentWithoutShare(final Activity context, final SocializeSession session, final Entity entity, final String text, final CommentAddListener listener) {
 		doCommentWithoutShare(context, session, entity, text, ShareUtils.getUserShareOptions(context), listener);
 	}
 	
 	protected void doCommentWithoutShare(final Activity context, final SocializeSession session, final Entity entity, final String text, final ShareOptions shareOptions, final CommentAddListener listener) {
 		final SafeProgressDialog progress = SafeProgressDialog.show(context, "Posting comment", "Please wait...");
 		commentSystem.addComment(session, entity, text, shareOptions, new CommentAddListener() {
 			@Override
 			public void onError(SocializeException error) {
 				progress.dismiss();
 				if(listener != null) {
 					listener.onError(error);
 				}
 			}
 			
 			@Override
 			public void onCreate(Comment comment) {
 				if(listener != null) {
 					listener.onCreate(comment);
 				}
 				if(shareOptions != null && shareOptions.getShareTo() != null) {
 					doActionShare(context, comment, text, progress, listener, shareOptions.getShareTo());
 				}
				else {
					if(progress != null) {
						progress.dismiss();
					}
				}
 			}
 		});		
 	}	
 
 	@Override
 	public void getComment(Activity context, long id, CommentGetListener listener) {
 		commentSystem.getComment(getSocialize().getSession(), id, listener);
 	}
 
 	@Override
 	public void getComments(Activity context, CommentListListener listener, long... ids) {
 		commentSystem.getCommentsById(getSocialize().getSession(), listener, ids);
 	}
 
 	@Override
 	public void getCommentsByUser(Activity context, User user, int start, int end, CommentListListener listener) {
 		commentSystem.getCommentsByUser(getSocialize().getSession(), user.getId(), start, end, listener);
 	}
 
 	@Override
 	public void getCommentsByEntity(Activity context, String entityKey, int start, int end, CommentListListener listener) {
 		commentSystem.getCommentsByEntity(getSocialize().getSession(), entityKey, start, end, listener);
 	}
 	
 	public void setCommentSystem(CommentSystem commentSystem) {
 		this.commentSystem = commentSystem;
 	}
 	
 	public void setAuthDialogFactory(IAuthDialogFactory authDialogFactory) {
 		this.authDialogFactory = authDialogFactory;
 	}
 
 	public void setShareDialogFactory(IShareDialogFactory shareDialogFactory) {
 		this.shareDialogFactory = shareDialogFactory;
 	}
 }
