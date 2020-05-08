 /**
  * 
  */
 package org.cotrix.web.ingest.client.step.preview;
 
 import java.util.List;
 
 import org.cotrix.web.common.client.error.ManagedFailureCallback;
 import org.cotrix.web.common.shared.DataWindow;
 import org.cotrix.web.ingest.client.IngestServiceAsync;
 import org.cotrix.web.ingest.client.util.PreviewDataGrid.PreviewDataProvider;
 import org.cotrix.web.ingest.shared.PreviewHeaders;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.Range;
 import com.google.inject.Inject;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class TablePreviewDataProvider implements PreviewDataProvider {
 
 	@Inject
 	private IngestServiceAsync service;
 
 	@Override
 	public void getHeaders(final AsyncCallback<PreviewHeaders> headersCallBack) {
 		service.getPreviewHeaders(null, new ManagedFailureCallback<PreviewHeaders>() {
 
 			@Override
 			public void onSuccess(PreviewHeaders result) {
				headersCallBack.onSuccess(result);
 			}
 		});
 	}
 
 	@Override
 	public void getData(Range range, final AsyncCallback<DataWindow<List<String>>> dataCallBack) {
 		service.getPreviewData(range, new ManagedFailureCallback<DataWindow<List<String>>>() {
 
 			@Override
 			public void onSuccess(DataWindow<List<String>> result) {
 				Log.trace("retrieved "+result);
 				dataCallBack.onSuccess(result);
 			}
 		});		
 	}
 
 }
