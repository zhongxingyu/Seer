 // Copyright (c) 2011, Chute Corporation. All rights reserved.
 //
 //  Redistribution and use in source and binary forms, with or without modification,
 //  are permitted provided that the following conditions are met:
 //
 //     * Redistributions of source code must retain the above copyright notice, this
 //       list of conditions and the following disclaimer.
 //     * Redistributions in binary form must reproduce the above copyright notice,
 //       this list of conditions and the following disclaimer in the documentation
 //       and/or other materials provided with the distribution.
 //     * Neither the name of the  Chute Corporation nor the names
 //       of its contributors may be used to endorse or promote products derived from
 //       this software without specific prior written permission.
 //
 //  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 //  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 //  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 //  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 //  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 //  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 //  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 //  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 //  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 //  OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 package com.chute.sdk.v2.api.upload;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import org.apache.http.client.methods.HttpPut;
 
import android.util.Log;

 import com.dg.libs.rest.client.BaseRestClient;
 import com.dg.libs.rest.entities.CountingInputStreamEntity;
 import com.dg.libs.rest.entities.CountingInputStreamEntity.UploadListener;
 import com.dg.libs.rest.exceptions.HttpException;
 
 public class GCS3Uploader extends BaseRestClient {
 
 	private static final int UPLOAD_SOCKET_TIMEOUT = 50 * 1000;
 
 	@SuppressWarnings("unused")
 	private static final String TAG = GCS3Uploader.class.getSimpleName();
 
 	private final UploadProgressListener onProgressUpdate;
 	private UploadToken token;
 
 	public GCS3Uploader(UploadProgressListener onProgressUpdate) {
 		this.onProgressUpdate = onProgressUpdate;
 	}
 
 	public void startUpload(UploadToken token) throws IOException {
 		this.token = token;
 		setUrl(token.getUploadInfo().getUploadUrl());
 		startUpload();
 	}
 
 	private void startUpload() throws IOException {
 		HttpPut request = new HttpPut(getUrl());
 		File file = new File(token.getUploadInfo().getFilepath());
 		FileInputStream fileInputStream = new FileInputStream(file);
		addHeader("Content-Length", String.valueOf(file.length()));
 		addHeader("Date", token.getUploadInfo().getDate());
 		addHeader("Authorization", token.getUploadInfo().getSignature());
 		addHeader("Content-Type", "image/jpg");
 		addHeader("x-amz-acl", "public-read");
 		CountingInputStreamEntity countingInputStreamEntity = new CountingInputStreamEntity(
 				fileInputStream, file.length());
 		final long total = file.length();
		Log.d("debug", "total = " + total);
 		countingInputStreamEntity.setUploadListener(new UploadListener() {
 
 			@Override
 			public void onChange(long current) {
 				onProgressUpdate.onProgress(total, current);
 			}
 		});
 		request.setEntity(countingInputStreamEntity);
 		executeRequest(request);
 	}
 
 	@Override
 	public void execute() throws HttpException {
 		// TODO Auto-generated method stub
 
 	}
 
 }
