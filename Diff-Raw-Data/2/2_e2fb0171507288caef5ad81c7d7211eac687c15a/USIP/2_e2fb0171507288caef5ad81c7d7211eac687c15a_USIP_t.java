 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 /*
  * Unlabeled data Sampling for Imbalanced Problem
  * 2クラスラベル付きのImbalanceなデータセットから、その分布を考慮して未ラベル事例をサンプリングするプログラム
  * k-NNにより密な分布を維持しつつ、サンプリングされた事例との距離を評価して全域のサンプルを抽出する。
  */
 public class USIP {
 	
 	// サンプリング数 (入力の未ラベルデータ数以下であること)
 	public static final int SAMPLING_NUM = 1000;
 	// k-NNに用いる定数 K (距離計算に用いる近傍数)
 	public static final int USE_NUM_OF_CALC_DIST = 3;
 	// 使う事例数 (事例が多すぎるときに指定すると計算コストが削減できる)
 	public static final int MAX_USE_INSTANCE_NUM = 10000;
    
 
     // 未ラベルデータファイル名
 	private String unlabelDataFile;
     // 少数派クラスデータファイル名
 	private String minorityDataFile;
 	
 	// 未ラベルデータ保持用配列
 	private ArrayList<Data> datas = null;
 	// 正例保持用配列
 	private ArrayList<Double> positives = null;
 	// サンプリングされた事例保持用配列
 	private ArrayList<Data> sampled = null;
 	
 	/*
 	 * コンストラクタ
 	 * 各配列の初期化を行います。
 	 */
 	public USIP(String dataFile, String posFile){
 	    unlabelDataFile = dataFile;
         minorityDataFile = posFile;
         datas = new ArrayList<Data>();
 		positives = new ArrayList<Double>();
 		sampled = new ArrayList<Data>();
 	}
 	
 	/*
 	 * 処理実行関数
 	 * クラスの生成元からはこの関数を呼び出します。
 	 */
 	public void run(){
 		readData();
 		calcDist();
 		execSampling();
 		output();
 	}
 	
 	/*
 	 * データ読み込み関数
 	 * この関数では以下のデータが読み込まれます。
 	 * 1. 未ラベル事例データファイル
 	 * 2. 正例データファイル
 	 */
 	private void readData(){
 		if( unlabelDataFile == null || minorityDataFile == null ){
 			System.err.println("[ERROR] Input files are not set to variables in class.");
 			System.exit(-1);
 		}
 
         // read unlabeled data
         System.out.println("[INFO] Reading unlabeled data.");
         BufferedReader br;
         try{
             br = new BufferedReader(new FileReader(unlabelDataFile));
             String line = null;
             int count = 0;
             while( (line=br.readLine()) != null ){
                ++count;
                String[] spLine = line.split(",");
                if( spLine.length == 2 )
                    datas.add(new Data(spLine[0], Double.parseDouble(spLine[1])));
                else
                    datas.add(new Data(String.valueOf(count), Double.parseDouble(spLine[0])));
             }
             br.close();
         }catch( Exception e){
             System.err.println("[ERROR] IO or Data format error at "+unlabelDataFile);
             e.printStackTrace();
             System.exit(-1);
         }
 
         // read minority class data
         System.out.println("[INFO] Reading minority class data.");
         try{
             br = new BufferedReader(new FileReader(minorityDataFile));
             String line = null;
             while( (line=br.readLine()) != null ){
                 String[] spLine = line.split(",");
                 positives.add(Double.parseDouble(spLine[spLine.length-1]));
             }
         }catch( Exception e){
             System.err.println("[ERROR] IO or Data format error at "+minorityDataFile);
             e.printStackTrace();
             System.exit(-1);
         }
 
 	    // output to stdout
         System.out.println("--------------------------------");
         System.out.println("- size of data       : "+datas.size());
         System.out.println("- size of postive    : "+positives.size());
         System.out.println("- k of k-NN          : "+USE_NUM_OF_CALC_DIST);
         System.out.println("- limit instance num : "+MAX_USE_INSTANCE_NUM);
         System.out.println("- sampling num       : "+SAMPLING_NUM);
         System.out.println("--------------------------------");
 		 
 	}
 	
 	/*
 	 * 未ラベル事例-正例間距離算出関数
 	 * 各未ラベル事例について、k-NNを用いてUSE_NUM_OF_CALC_DIST近傍との合計距離を求めます。
 	 */
 	private void calcDist(){
 		System.out.println("[INFO] Calculating distance between data and positive.");
 		for(int i=0; i<datas.size(); i++ ){
 			System.out.print("\r# processing "+(i+1)+" of "+datas.size()+" datas...");
 			ArrayList<Double> tmp_diffs = new ArrayList<Double>();
 			for(int j=0; j<positives.size(); j++ )
 				tmp_diffs.add(new Double(Math.abs(datas.get(i).getValue()-positives.get(j))));
 			Collections.sort(tmp_diffs);
 			double avgDist = 0;
 			for(int k=0; k<USE_NUM_OF_CALC_DIST; k++)
 				avgDist += tmp_diffs.get(k);
 			datas.get(i).setAvgPosDist(avgDist/USE_NUM_OF_CALC_DIST);
 			tmp_diffs = null;
 		}
 	}
 	
 	/*
 	 * サンプリング実行関数
 	 * サンプリングと評価値の再計算を繰り返し、SAMPLING_NUMの数だけサンプリングを行います。
 	 */
 	private void execSampling(){
 		System.out.println("\n[INFO] Execute Sampling");
 		int limit = Math.min(SAMPLING_NUM, datas.size());
 		for(int i=0; i<limit; i++){
 			Collections.sort(datas, new Comparator<Object>(){
 				public int compare(Object o1, Object o2){
 					if( ((Data)o1).getScore()-((Data)o2).getScore() == 0 )
 						return 0;
 					return ((Data)o1).getScore()-((Data)o2).getScore() < 0 ? 1 : -1;
 				}
 			});
 			System.out.print("\r# sampling "+(i+1)+" of "+limit+" datas ...");
 			Data smp = datas.remove(0);
 			sampled.add(smp);
 			//System.out.println(smp.getScore() +" : "+smp.getValue()+" : "+smp.getAvgPosDist()+" : "+smp.getSumSmpDist()); // debug
 			reCalcScore(smp.getValue());
 		}
 	}
 	
 	/*
 	 * 評価値再計算関数
 	 * 新たにサンプリングされた事例との距離を評価値に加えます。
 	 * @param
 	 * smpVal 新しくサンプリングされた事例の数値
 	 */
 	private void reCalcScore(double smpVal){
 		for(int i=0; i<datas.size(); i++){
 			datas.get(i).addSumSmpDist(Math.abs(smpVal-datas.get(i).getValue())); // 累計距離
			//datas.get(i).setSumSmpDist( ( datas.get(i).getSumSmpDist()*(sampled.size()-1)+Math.abs(smpVal-datas.get(i).getValue()) )/sampled.size() ); //平均距離
 		}
 	}
 	
 	/*
 	 * 結果出力関数
 	 * サンプリング結果を次の3つのファイルで出力します。
 	 * 1. *.feat : サンプリングされた事例の数値データのみ出力
 	 * 2. *.shot : サンプリングされた事例のIDのみ出力
 	 * 3. *.4ann : サンプリングされた事例のIDと数値データを半角空白区切りで出力
 	 */
 	private void output(){
         BufferedWriter bw_m, bw_s, bw_a;
         try{
                 bw_m = new BufferedWriter(new FileWriter(unlabelDataFile+".val"));
                 bw_s = new BufferedWriter(new FileWriter(unlabelDataFile+".ids"));
 				bw_a = new BufferedWriter(new FileWriter(unlabelDataFile+".smp"));
                 for(int i=0; i<sampled.size(); i++ ){                 
                         bw_m.write(sampled.get(i).getValue()+"\n");
                         bw_s.write(sampled.get(i).getId()+"\n");
                         bw_a.write(sampled.get(i).getId()+","+sampled.get(i).getValue()+"\n");
                 }
                 bw_m.close();
                 bw_s.close();
                 bw_a.close();
         }catch( Exception e){
                 System.err.println("[ERROR] Exception while outputting results\n");
                 e.printStackTrace();
                 System.exit(-1);
         }
         System.out.println("\n[FINISH] Results output to "+unlabelDataFile+".* ===");
 	}
 	
 	/*
 	 * メインルーチン
 	 * @param
 	 * 
 	 */
 	public static void main(String[] args){
 		
         if( args.length != 2 ){
             System.out.println("[USAGE] java USIP <UNLABELED_DATA_FILE> <MINORITY_CLASS_DATA_FILE>");
             System.exit(-1);
         }
         
 		USIP ins = new USIP(args[0], args[1]);
 		ins.run();
         
 	}
 	
 	/*
 	 * 未ラベルデータ用エレメント
 	 */
 	class Data {
 		
 		// データ識別用ID
 		private String id;
 		// データの値
 		private double value;
 		// 近傍の正例との距離
 		private double avgPosDist;
 		// 既にサンプリングされた事例との距離の和
 		private double sumSmpDist;
 		// 評価値(高いほどサンプリングされ、1つサンプリングされるごとに更新)
 		private double score;
 		
 		/*
 		 * コンストラクタ
 		 * @param
 		 * id データ識別用ID
 		 */
 		public Data(String id){
 			this.id = id;
 			this.value = 0;
 			this.sumSmpDist = 0;
 			this.avgPosDist = 0;
 			this.score = 0;
 		}
 		
 		/*
 		 * コンストラクタ
 		 * @param
 		 * id データ識別用ID
 		 * value データの値
 		 */
 		public Data(String id, double value){
 			this.id = id;
 			this.value = value;
 			this.sumSmpDist = 0;
 			this.avgPosDist = 0;
 			this.score = 0;
 		}
 		
 		public void setValue(double value){
 			this.value = value;
 			this.calcScore();
 		}
 		
 		public void setAvgPosDist(double avgPosDist){
 			this.avgPosDist = avgPosDist;
 			this.calcScore();
 		}
 		
 		public void setSumSmpDist(double sumSmpDist){
 			this.sumSmpDist = sumSmpDist;
 			this.calcScore();
 		}
 		
 		public void addSumSmpDist(double sumSmpDist){
 			this.sumSmpDist += sumSmpDist;
 			this.calcScore();
 		}
 		
 		/*
 		 * 評価値計算関数
 		 * 評価式はこの関数内に記述！
 		 */
 		private void calcScore(){
 			double sim = ( this.avgPosDist == 0 ) ? Math.log(1.0e+10) : Math.log(1/this.avgPosDist);
 			this.score = sim + this.sumSmpDist - this.value;
 		}
 		
 		public String getId(){
 			return this.id;
 		}
 		
 		public double getValue(){
 			return this.value;
 		}
 		
 		public double getAvgPosDist(){
 			return this.avgPosDist;
 		}
 		
 		public double getSumSmpDist(){
 			return this.sumSmpDist;
 		}
 		
 		public double getScore(){
 			return this.score;
 		}
 		
 	}
 
 }
