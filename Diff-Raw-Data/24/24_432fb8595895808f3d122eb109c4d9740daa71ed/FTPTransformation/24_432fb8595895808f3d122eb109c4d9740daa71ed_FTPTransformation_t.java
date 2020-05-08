 package com.hsun324.ftplite;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.hsun324.ftplite.FTPFile;
 
 public abstract class FTPTransformation<T> {
 	public static final FTPTransformation<FTPFile> FILE_TRANSFORMATION = new FTPTransformation<FTPFile>() {
 		@Override
 		public FTPFile transform(FTPState state, byte[] data) throws Exception {
 			if (state.typeImage) return new FTPFile(data);
 			return new FTPFile(data, FTPCharset.ASCII);
 		}
 	};
 	
 	private static final FTPEntity[] FTPENTITY_PROTOTYPE_ARRAY = new FTPEntity[0];
 
 	public static final FTPTransformation<String> ASCII_TRANSFORMATION = new FTPTransformation<String>() {
 		@Override
 		public String transform(FTPState state, byte[] data) throws Exception {
 			return new String(data, FTPCharset.ASCII);
 		}
 	};
 	
 	public static final FTPTransformation<FTPEntity[]> FILELIST_TRANSFORMATION = new FTPTransformation<FTPEntity[]> () {
 		@Override
 		public FTPEntity[] transform(FTPState state, byte[] data) throws Exception {
 			// TODO: proper newline splitting
			String[] lines = new String(data).split("\r\n?|\n\r?");
 			List<FTPEntity> list = new ArrayList<FTPEntity>();
 			int length = lines.length;
 			for (int i = 0; i < length; i++) {
 				FTPEntity entity = FTPEntity.parseEntity(state.workingDirectory, lines[i]);
 				if (entity != null) list.add(entity);
 			}
 			return list.toArray(FTPENTITY_PROTOTYPE_ARRAY);
 		}
 	};
 
 	public abstract T transform(FTPState state, byte[] data) throws Exception;
 }
