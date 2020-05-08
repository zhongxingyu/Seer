 package gr.ntua.image.mediachecker;
 
 public final class VideoInfo {
 
 	protected final int _Width, _Height, _BitRate;
 	protected final long _Duration;
	protected final String _MimeType, _FileFormat;
 	protected final double _FrameRate;
 
	public VideoInfo(int width, int height, String mimetype, String fileFormat, long duration, double framerate, int bitrate) {
 		_Width      = width;
 		_Height     = height;
 		_MimeType   = mimetype;
		_FileFormat = fileFormat;
 		_Duration   = duration;
 		_FrameRate  = framerate;
 		_BitRate    = bitrate;
 	}
 
 	public int getWidth() {
 		return _Width;
 	}
 
 	public int getHeight() {
 		return _Height;
 	}
 
 	public long getDuration() {
 		return _Duration;
 	}
 
 	public String getMimeType() {
 		return _MimeType;
 	}
 
	public String getFileFormat() {
		return _FileFormat;
 	}
 
 	public double getFrameRate() {
 		return _FrameRate;
 	}
 
 	public String getResolution() {
 		return _Width + "x" + _Height;
 	}
 }
