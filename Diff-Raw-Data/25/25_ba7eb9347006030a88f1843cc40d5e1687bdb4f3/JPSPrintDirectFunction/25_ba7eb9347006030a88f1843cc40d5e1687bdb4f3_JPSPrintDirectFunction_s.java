 package print;
 
 import java.io.FileInputStream;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 
 import javax.print.DocFlavor;
 import javax.print.DocPrintJob;
 import javax.print.PrintService;
 import javax.print.PrintServiceLookup;
 import javax.print.SimpleDoc;
 import javax.print.attribute.HashPrintRequestAttributeSet;
 import javax.print.attribute.PrintRequestAttributeSet;
 import javax.print.attribute.standard.JobName;
 import javax.print.attribute.standard.MediaSizeName;
 import javax.print.attribute.standard.PageRanges;
 
 public class JPSPrintDirectFunction extends JPSPFunction {
 
 	//private String path;
 
 	private DocFlavor df;
 
 	private SimpleDoc doc;
 
 	//	private String path;
 
 	public JPSPrintDirectFunction() {
 		super();
 	}
 
 	/**
 	 * 拡張子を設定するメソッド
 	 * @param xxx ファイル名
 	 */
 	private void setDf(String xxx) {
 		System.out.println("拡張子　：" + xxx);
 		if (xxx == null) {
 			df = null;
		} else switch (xxx) {
 		case "jpg":
 			df = DocFlavor.INPUT_STREAM.JPEG;
 			break;
 		case "png":
 			df = DocFlavor.INPUT_STREAM.PNG;
 			break;
 		case "gif":
 			df = DocFlavor.INPUT_STREAM.GIF;
 			break;
 		case "txt":
			df = DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_8;
 			break;
 		case "html":
 			df = DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_8;
 			break;
 		case "pdf":
 			df = DocFlavor.INPUT_STREAM.AUTOSENSE;
 			break;
 		//けーせｗｗｗ
 		case "kws":
 			df = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
 			break;
 		default:
 			df = null;
 		}
		
 	}
 
 	@Override
 	public boolean print() {
 		if (df == null)
 		{
 			System.out.println("対応していない拡張子です");
 			return false;
 		}
 		else {
 			try {
 
 				// 印刷要求属性
 				PrintRequestAttributeSet requestAttributeSet = new HashPrintRequestAttributeSet();
 				//紙の大きさ
 				requestAttributeSet.add(MediaSizeName.ISO_A4);
 				//			requestAttributeSet.add(Sides.TUMBLE);// ジョブ名
 				requestAttributeSet.add(new JobName("Print-" + getTime(), Locale.ENGLISH));
 
 				// 既定で選択される出力先
 				PrintService defaultService = PrintServiceLookup
 						.lookupDefaultPrintService();
 
 				// 印刷ジョブの生成
 				DocPrintJob job = defaultService.createPrintJob();
 				//ロゴ付きの時
 				if (df == DocFlavor.SERVICE_FORMATTED.PRINTABLE) {
 					//Docの生成
 					doc = new SimpleDoc(new KwsPrintable(path), df, null);
 					//１ページ
 					requestAttributeSet.add(new PageRanges("1"));
 				}
 				else {
 					//ストリームの生成
 					FileInputStream fin = new FileInputStream(path);
 					// 印刷ドキュメントの生成
 					doc = new SimpleDoc(fin, df, null);
 				} // ジョブに印刷を依頼する
 				job.print(doc, requestAttributeSet);
 			} catch (Exception e) {
 				e.printStackTrace();
 				return false;
 			}
 			return true;
 		}
 	}
 
 	/**
 	 * 印刷した時間の取得
 	 * @return 時間
 	 */
 	public String getTime() {
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat("mm:dd:ss");
 		return sdf.format(cal.getTime());
 	}
 
 	/**
 	 * 拡張子を取得するメソッド
 	 * @param fileName ファイルのパス
 	 * @return 拡張子
 	 */
 	private String getSuffix(String fileName) {
 		if (fileName == null)
 			return null;
 		int point = fileName.lastIndexOf(".");
 		if (point != -1) {
 			return fileName.substring(point + 1);
 		}
 		return fileName;
 	}
 
 	/**
 	 * ファイルのパスの指定
 	 */
 	@Override
 	public void setPath(String path) {
 		super.setPath(path);
 		setDf(getSuffix(path));
 	}
 }
