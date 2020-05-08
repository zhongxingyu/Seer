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
  * 
  * @author weiwei
  * 
  */
 public class ThumbUtil {
 
 	public static ByteArrayOutputStream generateThumb(String imagePath, String outputFormat, int failRetryTimes, long sleep, int outputWidth, int outputHeight) throws Exception {
 		return generateThumb(imagePath, 0, 0.9f, 0, 0, outputFormat, failRetryTimes, sleep, outputWidth, outputHeight);
 	}
 
 	/**
 	 * 注意！当宽度和高度都给定的情况下会进行裁剪。裁剪规则是：先按照比例压缩，然后将多出的部分分两边裁剪。
 	 * 
 	 * @param imagePath
 	 *            图片path，如果是以 http:// || https:// 开头则认为是远程图片
 	 * @param sharpenTimes 锐化次数
 	 * @param quality 质量 0f-1.0f
 	 * @param contrast
 	 *            对比度 默认1.2f
 	 * @param brightness
 	 *            亮度 默认 1.0f
 	 * @param outputFormat
 	 *            希望生成的缩略图格式
 	 * @param failRetryTimes
 	 *            图片获取失败尝试次数
 	 * @param sleep
 	 *            重试间隔时间 单位 毫秒
 	 * @param outputWidth
 	 *            希望生成的缩略图宽度
 	 * @param outputHeight
 	 *            希望生成的缩略图高度
 	 * @return
 	 * @throws Exception
 	 */
 	public static ByteArrayOutputStream generateThumb(String imagePath,
 			int sharpenTimes,
 			float quality, float contrast, float brightness,
 			String outputFormat, int failRetryTimes, long sleep,
 			int outputWidth, int outputHeight) throws Exception {
 		if (imagePath == null || imagePath.trim().length() == 0)
 			throw new Exception("ImageURL required");
 
 		if (outputFormat == null || outputFormat.trim().length() == 0)
 			outputFormat = imagePath.substring(imagePath.lastIndexOf(".") + 1,
 					imagePath.length());
 
 		if (outputFormat == null || outputFormat.trim().length() == 0)
 			throw new Exception("can not get the image suffix -> " + imagePath);
 
 		if (failRetryTimes <= 0)
 			failRetryTimes = 1;
 
 		final String W = "width";
 		final String H = "height";
 
 		BufferedImage bi = null;
 		try {
 			bi = FileUtil.getBufferedImage(imagePath, failRetryTimes, sleep);
 		} catch (Exception e) {
 			throw e;
 		}
 
 		if (bi == null)
 			throw new Exception("can not get the image file from -> "
 					+ imagePath);
 
 		//原图大小
 		int sw = bi.getWidth();
 		int sh = bi.getHeight();
 		
 		// 如果原图比目标长宽要少，用原图大小,这样就不会进行放大了
 		if (sw < outputWidth)
 			outputWidth = sw;
 		if (sh < outputHeight)
 			outputHeight = sh;
 
 		// 原图宽高
 		final Map<String, Integer> source = new HashMap<String, Integer>();
 		source.put(W, sw);
 		source.put(H, sh);
 
 		// 如果给出的长宽不大于0的话，用原图大小
 		if (outputWidth <= 0 && outputHeight <= 0) {
 			outputWidth = sw;
 			outputHeight = sh;
 		}
 
 		// 目标宽高
 		final Map<String, Integer> output = new HashMap<String, Integer>();
 		if (outputWidth > 0)
 			output.put(W, outputWidth);
 
 		if (outputHeight > 0)
 			output.put(H, outputHeight);
 		
 		// 比较W与H，找出小的，记住小的那个
 		String min = W;
 		if (sh < sw)
 			min = H;
 		// 如果小值不存在，则小值取给过来的其中一个值
 		if (!output.containsKey(min)) {
 			if (output.containsKey(W))
 				min = W;
 			else
 				min = H;
 		}
 
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 
 		// 锐化
 		if (sharpenTimes > 0)
 			bi = sharpen(bi, sharpenTimes);
 
 		// 对比度、亮度过滤
 		if (contrast > 0 || brightness > 0) {
 			ContrastFilter filter = new ContrastFilter();
 			if (contrast > 0)
 				filter.setContrast(contrast);
 			if (brightness > 0)
 				filter.setBrightness(brightness);
 			bi = filter.filter(bi, null);
 		}
 
 		// 如果给了两个参数，则剪裁
 		if (output.containsKey(W) && output.containsKey(H)) {
 			
			// 裁剪的话因为要保留最大区域所以按照比例最小的那端进行裁剪
			double scale ;
			double wScale = Double.parseDouble(String.valueOf(source.get(W))) / Double.parseDouble(String.valueOf(output.get(W)));
			double hScale = Double.parseDouble(String.valueOf(source.get(H))) / Double.parseDouble(String.valueOf(output.get(H)));
			if (wScale < hScale)
				scale = wScale;
			else
				scale = hScale;
			
			int sW = new Double(source.get(W) / scale).intValue();
			int sH = new Double(source.get(H) / scale).intValue();
			
 			BufferedImage _bi = Thumbnails.of(bi).size(sW, sH).outputFormat(outputFormat).asBufferedImage();
 
 			// scale必须为 1 的时候图片才不被放大
 			Thumbnails
 					.of(_bi)
 					.scale(1)
 					.sourceRegion(Positions.CENTER, output.get(W),
 							output.get(H)).outputQuality(quality)
 					.outputFormat(outputFormat).toOutputStream(os);
 
 		} else {
 			// 算出比例
			double scale = Double.parseDouble(String.valueOf(source.get(min))) / Double.parseDouble(String.valueOf(output.get(min)));
 			int sW = new Double(sw / scale).intValue();
 			int sH = new Double(sh / scale).intValue();
 			// 压缩
 			Thumbnails.of(bi).size(sW, sH).outputQuality(quality)
 					.outputFormat(outputFormat).toOutputStream(os);
 		}
 
 		return os;
 	}
 
 	//锐化
 	public static BufferedImage sharpen(BufferedImage src) {
 		return sharpen(src, 2);
 	}
 	
 	public static BufferedImage sharpen(BufferedImage src, int times) {
         BufferedImage desc = null;
         SharpenFilter filter = new SharpenFilter();
         filter.setUseAlpha(true);
         desc = filter.filter(src, desc);
         for (int i = 0; i < times - 1; i++) {
             desc = filter.filter(desc, desc);
         }
         filter = null;
  
         return desc;
     }
 
 	public static void main(String[] args) throws Exception {
 
 		//锐化次数
 		int sharpenTimes = 0;
 		
 		// 质量
 		float quality = 0.9f;
 		String outputFormat = "jpg";
 		String name = CommonUtil.getNowTime("yyyyMMddHHmmss");
 
 		// 原图，也可以是本地的d:/xx.jpg
		//String remoteImageUrl = "http://d10abt682efc89.cloudfront.net/p/evie-1713-20557-1-zoom.jpg";
//		String remoteImageUrl = "http://gd.image-gmkt.com/mi/830/443/414443830.jpg";
		String remoteImageUrl = "http://test.shoplay.com/cache/bigpic/20121108/470/55c5b78e5c_w470.jpg";
		int outputWidth = 210;
		int outputHeight = 250;
 
 		float contrast = 0f; // 对比度
 		float brightness = 0f; // 亮度 0 表示不调整
 
 		File file = new File("d:/" + name + "_w" + outputWidth + "h" + outputHeight + "_sharpen" + sharpenTimes 
 				+ "_contrat" + contrast + "_quality"+quality + "." + outputFormat);
 
 		ByteArrayOutputStream os = ThumbUtil.generateThumb(
 				remoteImageUrl, 
 				sharpenTimes,
 				quality, contrast, brightness, outputFormat, 1, // 远程图片下载失败重试次数
 				1 * 1000, // 失败后休眠时间
 				outputWidth, outputHeight);
 
 		FileOutputStream writer = new FileOutputStream(file);
 		writer.write(os.toByteArray());
 
 		File _f = new File(file.getAbsolutePath());
 		System.out.println("generate file -> " + _f.getAbsolutePath() + " "
 				+ _f.exists());
 	}
 
 }
