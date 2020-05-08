 package fileObserve;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.print.PrintException;
 
 import print.JPSPrintDirectFunction;
 
 /**
  * ファイルの監視をするクラス
  * @author QB
  *
  */
 public class AutoDirCheck {

 	// 更新の監視対象となるディレクトリ
 	private File targetDir;
 	//終了のフラグ
 	protected boolean fStop;
 	//
 	protected List<String> fRegistereds;
 	//印刷機能を提供する抽象クラス
 	private JPSPrintDirectFunction jsp;

 	public AutoDirCheck() {
 		Config config = new Config("config.properties");
 		String path = config.read("path");
 		targetDir = new File(path);
 	}
 
 	/**
 	 * 更新チェックの開始
 	 */
 	public void start() {
 		fStop = false;
 		fRegistereds = new ArrayList<String>();
 		jsp = new JPSPrintDirectFunction();
 		Thread thread = new Thread(new AutoChecker());
 		thread.start();
 		System.out.println("監視を始めます");
 	}
 
 	/**
 	 * 更新チェックの終了
 	 */
 	public void stop() {
 		fStop = true;
 	}
 
 	/**
 	 * チェック処理
 	 */
 	protected void check() {
 		//削除の判定
 		checkRemoved();
 		// 新規ファイルの判定
 		checkNew();
 	}
 
 	/**
 	 * 削除されたファイルのチェック
 	 */
 	protected void checkRemoved() {
 		Iterator<String> it = fRegistereds.iterator();
 		while (it.hasNext()) {
 			String filename = it.next();
 			File file = new File(targetDir, filename);
 			if (!file.exists()) {
 				// 削除処理
 				it.remove();
 				System.out.println(filename + " が削除されました");
 			}
 		}
 	}
 
 	/**
 	 * 新たに追加されたファイルのチェック
 	 * @throws PrintException
 	 */
 	protected void checkNew() {
 		String[] files = targetDir.list();
 		for (int i = 0; i < files.length; i++) {
 			if (!fRegistereds.contains(files[i])) {
 				// 追加処理
 				fRegistereds.add(files[i]);
 				System.out.println(files[i] + " が追加されました");
 				//印刷
				File targetFile = new File(targetDir, files[i]);
 				jsp.setPath(targetFile.toString());
 				if (jsp.print()) {
 					System.out.println("印刷成功");
					targetFile.delete(); // ファイル削除
 				} else {
 					System.out.println("印刷失敗");
 				}
 			}
 		}
 	}
 
 	/**
 	 *	更新チェックスレッド
 	 */
 	protected class AutoChecker implements Runnable {
 
 		@Override
 		public void run() {
 			while (!fStop) {
 				try {
 					Thread.sleep(1000L); // チェック間隔
 				} catch (InterruptedException e) {
 				}
 				check();
 			}
 		}
 	}
 }
