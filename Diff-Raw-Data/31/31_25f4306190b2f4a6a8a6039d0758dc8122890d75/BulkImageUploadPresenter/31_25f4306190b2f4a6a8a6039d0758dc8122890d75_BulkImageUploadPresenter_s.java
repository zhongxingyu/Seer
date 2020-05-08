 package com.redhat.topicindex.extras.client.local.presenter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.enterprise.context.Dependent;
 import javax.inject.Inject;
 
 import org.jboss.errai.bus.client.api.ErrorCallback;
 import org.jboss.errai.bus.client.api.Message;
 import org.jboss.errai.bus.client.api.RemoteCallback;
 import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTLanguageImageCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTImageV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTLanguageImageV1;
 import org.jboss.pressgang.ccms.rest.v1.jaxrsinterfaces.RESTInterfaceV1;
 import org.vectomatic.file.File;
 import org.vectomatic.file.FileList;
 import org.vectomatic.file.FileReader;
 import org.vectomatic.file.events.ErrorHandler;
 import org.vectomatic.file.events.LoadEndEvent;
 import org.vectomatic.file.events.LoadEndHandler;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.redhat.topicindex.extras.client.local.Presenter;
 import com.redhat.topicindex.extras.client.local.constants.Constants;
 import com.redhat.topicindex.extras.client.local.utilities.GWTUtilities;
 import com.redhat.topicindex.extras.client.local.view.ImageUploadData;
 import com.smartgwt.client.widgets.Progressbar;
 
 @Dependent
 public class BulkImageUploadPresenter implements Presenter
 {
 	private static final String IMAGE_COLLECTION_EXPAND = "{\"branches\": [{\"trunk\": {\"name\": \"languageImages\"}}]}";
 	private static final String REST_SERVER = Constants.BASE_URL + "seam/resource/rest";
 	
 	//@Inject
 	//private HandlerManager eventBus;
 
 	@Inject
 	private Display display;
 	
 	public interface Display
 	{
 		List<ImageUploadData> getImageUploadBlocks();
 		Button getNewLangButton();
 		Button getUpload();
 		TextArea getFinalResults();
 		TextBox getPrefix();
 		VerticalPanel getVerticalPanel();
 		Progressbar getProgress();
 		Widget asWidget();
 	}
 	
 	@Override
 	public void go(HasWidgets container)
 	{
 		/* Init the REST service */
 		RestClient.setApplicationRoot(REST_SERVER);
 		RestClient.setJacksonMarshallingActive(true);
 		
 		bind();
 		container.clear();
 		container.add(display.asWidget());
 	}
 	
 	private void bind()
 	{
 		display.getNewLangButton().addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(final ClickEvent event)
 			{
 				final ImageUploadData newBlock = new ImageUploadData();
 				display.getImageUploadBlocks().add(newBlock);
 				display.getVerticalPanel().add(newBlock.getGrid());
 				
 				newBlock.getUpload().addChangeHandler(new ChangeHandler()
 				{
 					@Override
 					public void onChange(final ChangeEvent event)
 					{
 						final FileList files = newBlock.getUpload().getFiles();
 						final StringBuilder text = new StringBuilder();
 						for (final File file : files)
 						{
 							text.append(file.getName() + "\n");
 						}
 						newBlock.getFileList().setText(text.toString());
 					}
 				});
 			}
 		});
 		
 		display.getUpload().addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				final List<String> fileNames = getUniqueFileNames();
 				if (fileNames.size() != 0)
 				{
 					setEnabled(false);
 
 					display.getFinalResults().setText("");
 					display.getFinalResults().setVisible(false);
 
 					display.getProgress().setVisible(true);
 
 					final StringBuilder results = new StringBuilder();
 					final List<RESTImageV1> images = new ArrayList<RESTImageV1>();
 					processFile(0, fileNames, results, images);
 				}
 			}
 		});
 	}
 	
 	private List<String> getUniqueFileNames()
 	{
 		final List<String> retValue = new ArrayList<String>();
 
 		for (final ImageUploadData data : display.getImageUploadBlocks())
 		{
 			for (final File file : data.getUpload().getFiles())
 			{
 				if (!retValue.contains(data))
 				{
 					if (!retValue.contains(file.getName()))
 						retValue.add(file.getName());
 				}
 			}
 		}
 
 		return retValue;
 	}
 	
 	private void processRESTImage(final int index, final StringBuilder results, final List<RESTImageV1> images)
 	{
 		if (index >= images.size())
 		{
 			System.out.println("Progress [UPLOAD] 100%");
 			reEnabledUI(results);
 		}
 		else
 		{
 
 			final RemoteCallback<RESTImageV1> successCallback = new RemoteCallback<RESTImageV1>()
 			{
 				@Override
 				public void callback(final RESTImageV1 image)
 				{
 					final int progressValue = (int) ((float) index / images.size() * 50) + 50;
 					display.getProgress().setPercentDone(progressValue);
 
 					System.out.println("Progress [UPLOAD] " + progressValue + "%");
 
 					/* output a mapping of file names to image ids */
 					if (image.getLanguageImages_OTM() != null && image.getLanguageImages_OTM().getItems() != null)
 					{
 					    final List<RESTLanguageImageV1> langImages = image.getLanguageImages_OTM().returnItems();
 						for (final RESTLanguageImageV1 langImage : langImages)
 						{
 							results.append("[" + langImage.getLocale() + "] " + image.getId() + ": " + langImage.getFilename() + "\n");
 							processRESTImage(index + 1, results, images);
 						}
 					}
 				}
 			};
 
 			final ErrorCallback errorCallback = new ErrorCallback()
 			{
 				@Override
 				public boolean error(Message message, Throwable throwable)
 				{
 					results.append("Upload was a failure!");
 					reEnabledUI(results);
 					return true;
 				}
 			};
 
 			final RESTInterfaceV1 restMethod = RestClient.create(RESTInterfaceV1.class, successCallback, errorCallback);
 
 			try
 			{
 
 				final RESTImageV1 image = images.get(index);
 				restMethod.createJSONImage(IMAGE_COLLECTION_EXPAND, image);
 			}
 			catch (final Exception ex)
 			{
 				results.append("Upload was a failure!\n");
 				results.append(ex.toString());
 				processRESTImage(index + 1, results, images);
 			}
 		}
 	}
 
 	private void reEnabledUI(final StringBuilder results)
 	{
 		display.getFinalResults().setText(results.toString());
 		display.getFinalResults().setVisible(true);
 		display.getProgress().setVisible(false);
 		setEnabled(true);
 	}
 
 	private void processLanguageImage(final int blockIndex, final int fileIndex, final List<String> fileNames, final StringBuilder results, final RESTImageV1 image, final File file, final RESTLanguageImageV1 langImg, final List<RESTImageV1> images)
 	{
 		final FileReader reader = new FileReader();
 
 		reader.addErrorHandler(new ErrorHandler()
 		{
 			@Override
 			public void onError(org.vectomatic.file.events.ErrorEvent event)
 			{
 				processBlock(blockIndex + 1, fileIndex, fileNames, results, image, images);
 			}
 		});
 
 		reader.addLoadEndHandler(new LoadEndHandler()
 		{
 			@Override
 			public void onLoadEnd(LoadEndEvent event)
 			{
 				final ImageUploadData block = display.getImageUploadBlocks().get(blockIndex);
 
 				final String result = reader.getStringResult();
 				final byte[] buffer = GWTUtilities.getByteArray(result, 1);
 
 				langImg.explicitSetImageData(buffer);
 				langImg.explicitSetLocale(block.getLanguage().getValue(block.getLanguage().getSelectedIndex()));
 				langImg.explicitSetFilename(display.getPrefix().getText() + file.getName());
 
 				processBlock(blockIndex + 1, fileIndex, fileNames, results, image, images);
 			}
 		});
 
 		reader.readAsBinaryString(file);
 	}
 
 	private void processBlock(final int blockIndex, final int fileIndex, final List<String> fileNames, final StringBuilder results, final RESTImageV1 image, final List<RESTImageV1> images)
 	{
 		if (blockIndex >= display.getImageUploadBlocks().size())
 		{
 			processFile(fileIndex + 1, fileNames, results, images);
 		}
 		else
 		{
 			final String filename = fileNames.get(fileIndex);
 			final ImageUploadData block = display.getImageUploadBlocks().get(blockIndex);
 
 			for (final File file : block.getUpload().getFiles())
 			{
 				if (file.getName().equals(filename))
 				{
 					final RESTLanguageImageV1 langImg = new RESTLanguageImageV1();
 					image.getLanguageImages_OTM().addNewItem(langImg);
 					processLanguageImage(blockIndex, fileIndex, fileNames, results, image, file, langImg, images);
 
 					break;
 				}
 			}
 		}
 	}
 
 	private void processFile(final int fileIndex, final List<String> fileNames, final StringBuilder results, final List<RESTImageV1> images)
 	{
 		if (fileIndex >= fileNames.size())
 		{
 			// uploadDone(results, images);
 			System.out.println("Progress [READING]: 100%");
 			System.out.println("Progress [UPLOADING]");
 			processRESTImage(0, results, images);
 		}
 		else
 		{
 			final int progressValue = (int) ((float) fileIndex / fileNames.size() * 50);
 			display.getProgress().setPercentDone(progressValue);
 
 			System.out.println("Progress [READING]: " + progressValue + "%");
 
 			final RESTImageV1 image = new RESTImageV1();
 			image.explicitSetLanguageImages_OTM(new RESTLanguageImageCollectionV1());
 			images.add(image);
 			processBlock(0, fileIndex, fileNames, results, image, images);
 		}
 	}
 
 	/**
 	 * Set the state of the UI elements
 	 * 
 	 * @param enabled
 	 *            true if the elements are to be enabled, false otherwise
 	 */
 	private void setEnabled(final boolean enabled)
 	{
 		for (final ImageUploadData block : display.getImageUploadBlocks())
 		{
 			block.setEnabled(enabled);
 		}
 
 		display.getFinalResults().setEnabled(enabled);
 		display.getPrefix().setEnabled(enabled);
 		display.getUpload().setEnabled(enabled);
 		display.getNewLangButton().setEnabled(enabled);
 	}
 
 
 }
