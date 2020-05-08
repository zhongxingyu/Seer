 package org.eweb4j.util;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.coobird.thumbnailator.Thumbnails;
 import net.coobird.thumbnailator.geometry.Positions;
 
 /**
  * 缩略图工具类
  * @author weiwei
  *
  */
 public class ThumbUtil {
 	
 	public static ByteArrayOutputStream generateThumb(String imagePath, String outputFormat, int failRetryTimes, long sleep, int outputWidth, int outputHeight) throws Exception{
 		return generateThumb(imagePath, 0, 0, outputFormat, failRetryTimes, sleep, outputWidth, outputHeight);
 	}
 	
 	/**
 	 * 注意！当宽度和高度都给定的情况下会进行裁剪。裁剪规则是：先按照比例压缩，然后将多出的部分分两边裁剪。
 	 * @param imagePath 图片path，如果是以 http:// || https:// 开头则认为是远程图片
 	 * @param contrast 对比度 默认1.2f
 	 * @param brightness 亮度 默认 1.0f
 	 * @param outputFormat 希望生成的缩略图格式
 	 * @param failRetryTimes 图片获取失败尝试次数
 	 * @param sleep 重试间隔时间 单位 毫秒
 	 * @param outputWidth 希望生成的缩略图宽度
 	 * @param outputHeight 希望生成的缩略图高度
 	 * @return
 	 * @throws Exception
 	 */
 	public static ByteArrayOutputStream generateThumb(
 			String imagePath, float contrast, float brightness, String outputFormat, 
 			int failRetryTimes, long sleep, int outputWidth, int outputHeight) throws Exception{
 		if (imagePath == null || imagePath.trim().length() == 0)
 			throw new Exception("ImageURL required");
 		
 		if (outputFormat == null || outputFormat.trim().length() == 0)
 			outputFormat = imagePath.substring(imagePath.lastIndexOf(".")+1, imagePath.length());
 		
 		if (outputFormat == null || outputFormat.trim().length() == 0)
 			throw new Exception("can not get the image suffix -> " + imagePath);
 		
 		if (failRetryTimes <= 0)
 			failRetryTimes = 1;
 		
 		final String W = "width";
 		final String H = "height";
 		
 		BufferedImage bi = null;
 		try {
 			bi = FileUtil.getBufferedImage(imagePath, failRetryTimes, sleep);
 		}catch(Exception e){
 			throw e;
 		}
 		
 		if (bi == null)
 			throw new Exception("can not get the image file from -> " + imagePath);
 		
 		int w = bi.getWidth();
 		int h = bi.getHeight();
 		
 		//如果原图比目标长宽要少，用原图大小,这样就不会进行放大了
 		if (w < outputWidth)
 			outputWidth = w;
 		if (h < outputHeight)
 			outputHeight = h;
 		
 		//原图宽高
 		final Map<String,Integer> source = new HashMap<String,Integer>();
 		source.put(W, w);
 		source.put(H, h);
 		
 		//比较W与H，找出小的，记住小的那个
 		//如果给出的长宽不大于0的话，用原图大小
 		if (outputWidth <= 0 && outputHeight <= 0){
 			outputWidth = w;
 			outputHeight = h;
 		}
 		
 		//目标宽高
 		final Map<String,Integer> output = new HashMap<String, Integer>();
 		if (outputWidth > 0)
 			output.put(W, outputWidth);
 		
 		if (outputHeight > 0)
 			output.put(H, outputHeight);
 		
 		String min = W;
 		if (h < w)
 			min = H;
 		// 如果小值不存在，则小值取给过来的其中一个值
 		if (!output.containsKey(min)){
 			if (output.containsKey(W))
 				min = W;
 			else
 				min = H;
 		}
 		
 		//算出比例
 		double scale = (double)source.get(min)/output.get(min);
 		int sW = new Double(w/scale).intValue();
 		int sH = new Double(h/scale).intValue();
 		
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		
 		//对比度、亮度过滤
		ImageFilter filter = new ImageFilter();
		if (contrast > 0)
			filter.setContrast(contrast);
		if (brightness > 0)
			filter.setBrightness(brightness);
		if (contrast > 0 || brightness > 0)
 			bi = filter.filter(bi, null);
 		
 		//如果给了两个参数，则剪裁
 		if (output.containsKey(W) && output.containsKey(H)){
 			//压缩
 			BufferedImage _bi = 
 				Thumbnails.of(bi).size(sW, sH)
 					.outputFormat(outputFormat)
 					.asBufferedImage();
 			
 			//scale必须为 1 的时候图片才不被放大
 			Thumbnails.of(_bi).scale(1)
 				.sourceRegion(Positions.CENTER, output.get(W), output.get(H))
 				.outputFormat(outputFormat)
 				.toOutputStream(os);
 			
 		}else{
 			//压缩
 			Thumbnails.of(bi).size(sW, sH)
 				.outputFormat(outputFormat)
 				.toOutputStream(os);
 		}
 		
 		return os;
 	}
 	
 	public static void main(String[] args) throws Exception{
 		String outputFormat = "jpg";
 		String name = CommonUtil.getNowTime("yyyyMMddHHmmss");
 		String remoteImageUrl = "http://static.zalora.sg/p/palette-8494-03928-1-zoom.jpg";
 		int outputWidth = 210;
 		int outputHeight = 0;
 		
 		File file = new File("d:/"+name+"_w"+outputWidth+"h"+outputHeight+"."+outputFormat);
 		
 		ByteArrayOutputStream os = ThumbUtil.generateThumb(remoteImageUrl,outputFormat, 1, 1*1000, outputWidth, outputHeight);
 		FileOutputStream writer = new FileOutputStream(file);
 		writer.write(os.toByteArray());
 		File _f = new File(file.getAbsolutePath());
 		System.out.println("generate file -> " + _f.getAbsolutePath() + " " + _f.exists());
 	}
 	
 }
