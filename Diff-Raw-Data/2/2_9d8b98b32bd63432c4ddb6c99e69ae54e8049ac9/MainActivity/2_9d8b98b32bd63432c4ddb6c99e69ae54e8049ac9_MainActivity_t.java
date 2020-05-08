 package biz.lungo.listviewtask;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.Window;
 import android.widget.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class MainActivity extends Activity {
     static final int MAX_SEEK_BAR_VALUE = 10;
     static final String SPEED = "Speed: ";
     final String[] names = {"1. Smith", "2. Johnson", "3. Williams ", "4. Jones", "5. Brown", "6. Davis", "7. Miller",
             "8. Wilson", "9. Moore", "10. Taylor", "11. Anderson", "12. Thomas", "13. Jackson ", "14. White",
             "15. Harris", "16. Martin", "17. Thompson", "18. Garcia", "19. Martinez", "20. Robinson", "21. Clark",
             "22. Rodriguez", "23. Lewis", "24. Lee", "25. Walker", "26. Hall", "27. Allen", "28. Young",
             "29. Hernandez", "30. King", "31. Wright", "32. Lopez", "33. Hill", "34. Scott", "35. Green", "36. Adams",
             "37. Baker", "38. Gonzalez", "39. Nelson", "40. Carter", "41. Mitchell", "42. Perez", "43. Roberts",
             "44. Turner", "45. Phillips", "46. Campbell", "47. Parker", "48. Evans", "49. Edwards", "50. Collins",
             "51. Smith", "52. Johnson", "53. Williams ", "54. Jones", "55. Brown", "56. Davis", "57. Miller",
             "58. Wilson", "59. Moore", "60. Taylor", "61. Anderson", "62. Thomas", "63. Jackson ", "64. White",
             "65. Harris", "66. Martin", "67. Thompson", "68. Garcia", "69. Martinez", "70. Robinson", "71. Clark",
             "72. Rodriguez", "73. Lewis", "74. Lee", "75. Walker", "76. Hall", "77. Allen", "78. Young", "79. Hernandez",
             "80. King", "81. Wright", "82. Lopez", "83. Hill", "84. Scott", "85. Green", "86. Adams", "87. Baker",
             "88. Gonzalez", "89. Nelson", "90. Carter", "91. Mitchell", "92. Perez", "93. Roberts", "94. Turner",
             "95. Phillips", "96. Campbell", "97. Parker", "98. Evans", "99. Edwards", "100. Collins", "101. Smith",
             "102. Johnson", "103. Williams ", "104. Jones", "105. Brown", "106. Davis", "107. Miller", "108. Wilson",
             "109. Moore", "110. Taylor", "111. Anderson", "112. Thomas", "113. Jackson ", "114. White", "115. Harris",
             "116. Martin", "117. Thompson", "118. Garcia", "119. Martinez", "120. Robinson", "121. Clark", "122. Rodriguez",
             "123. Lewis", "124. Lee", "125. Walker", "126. Hall", "127. Allen", "128. Young", "129. Hernandez", "130. King",
             "131. Wright", "132. Lopez", "133. Hill", "134. Scott", "135. Green", "136. Adams", "137. Baker",
             "138. Gonzalez", "139. Nelson", "140. Carter", "141. Mitchell", "142. Perez", "143. Roberts", "144. Turner",
             "145. Phillips", "146. Campbell", "147. Parker", "148. Evans", "149. Edwards", "150. Collins",
             "151. Smith", "152. Johnson", "153. Williams ", "154. Jones", "155. Brown", "156. Davis", "157. Miller",
             "158. Wilson", "159. Moore", "160. Taylor", "161. Anderson", "162. Thomas", "163. Jackson ", "164. White",
             "165. Harris", "166. Martin", "167. Thompson", "168. Garcia", "169. Martinez", "170. Robinson", "171. Clark",
             "172. Rodriguez", "173. Lewis", "174. Lee", "175. Walker", "176. Hall", "177. Allen", "178. Young",
             "179. Hernandez", "180. King", "181. Wright", "182. Lopez", "183. Hill", "184. Scott", "185. Green",
             "186. Adams", "187. Baker", "188. Gonzalez", "189. Nelson", "190. Carter", "191. Mitchell", "192. Perez",
             "193. Roberts", "194. Turner", "195. Phillips", "196. Campbell", "197. Parker", "198. Evans", "199. Edwards",
             "200. Collins", "201. Smith", "202. Johnson", "203. Williams ", "204. Jones", "205. Brown", "206. Davis",
             "207. Miller", "208. Wilson", "209. Moore", "210. Taylor", "211. Anderson", "212. Thomas", "213. Jackson ",
             "214. White", "215. Harris", "216. Martin", "217. Thompson", "218. Garcia", "219. Martinez", "220. Robinson",
             "221. Clark", "222. Rodriguez", "223. Lewis", "224. Lee", "225. Walker", "226. Hall", "227. Allen",
             "228. Young", "229. Hernandez", "230. King", "231. Wright", "232. Lopez", "233. Hill", "234. Scott",
             "235. Green", "236. Adams", "237. Baker", "238. Gonzalez", "239. Nelson", "240. Carter", "241. Mitchell",
             "242. Perez", "243. Roberts", "244. Turner", "245. Phillips", "246. Campbell", "247. Parker", "248. Evans",
             "249. Edwards", "250. Collins", "251. Smith", "252. Johnson", "253. Williams ", "254. Jones", "255. Brown",
             "256. Davis", "257. Miller", "258. Wilson", "259. Moore", "260. Taylor", "261. Anderson", "262. Thomas",
             "263. Jackson ", "264. White", "265. Harris", "266. Martin", "267. Thompson", "268. Garcia", "269. Martinez",
             "270. Robinson", "271. Clark", "272. Rodriguez", "273. Lewis", "274. Lee", "275. Walker", "276. Hall",
             "277. Allen", "278. Young", "279. Hernandez", "280. King", "281. Wright", "282. Lopez", "283. Hill",
             "284. Scott", "285. Green", "286. Adams", "287. Baker", "288. Gonzalez", "289. Nelson", "290. Carter",
             "291. Mitchell", "292. Perez", "293. Roberts", "294. Turner", "295. Phillips", "296. Campbell", "297. Parker",
             "298. Evans", "299. Edwards", "300. Collins", "301. Smith", "302. Johnson", "303. Williams ", "304. Jones",
             "305. Brown", "306. Davis", "307. Miller", "308. Wilson", "309. Moore", "310. Taylor", "311. Anderson",
             "312. Thomas", "313. Jackson ", "314. White", "315. Harris", "316. Martin", "317. Thompson", "318. Garcia",
             "319. Martinez", "320. Robinson", "321. Clark", "322. Rodriguez", "323. Lewis", "324. Lee", "325. Walker",
             "326. Hall", "327. Allen", "328. Young", "329. Hernandez", "330. King", "331. Wright", "332. Lopez",
             "333. Hill", "334. Scott", "335. Green", "336. Adams", "337. Baker", "338. Gonzalez", "339. Nelson",
             "340. Carter", "341. Mitchell", "342. Perez", "343. Roberts", "344. Turner", "345. Phillips", "346. Campbell",
             "347. Parker", "348. Evans", "349. Edwards", "350. Collins", "351. Smith", "352. Johnson", "353. Williams ",
             "354. Jones", "355. Brown", "356. Davis", "357. Miller", "358. Wilson", "359. Moore", "360. Taylor",
             "361. Anderson", "362. Thomas", "363. Jackson ", "364. White", "365. Harris", "366. Martin", "367. Thompson",
             "368. Garcia", "369. Martinez", "370. Robinson", "371. Clark", "372. Rodriguez", "373. Lewis", "374. Lee",
             "375. Walker", "376. Hall", "377. Allen", "378. Young", "379. Hernandez", "380. King", "381. Wright",
             "382. Lopez", "383. Hill", "384. Scott", "385. Green", "386. Adams", "387. Baker", "388. Gonzalez",
             "389. Nelson", "390. Carter", "391. Mitchell", "392. Perez", "393. Roberts", "394. Turner", "395. Phillips",
             "396. Campbell", "397. Parker", "398. Evans", "399. Edwards", "400. Collins"};
     int progress;
     int counter;
     boolean isRun;
     Thread listThread;
     Handler handlerText;
     Handler handlerList;
     ListView listView;
     TextView textView;
     Button showList;
     Button hideList;
     SeekBar seekBar;
     ArrayList<String> namesList = new ArrayList<String>(Arrays.asList(names));
     ArrayAdapter<String> namesAdapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.activity_main);
         showList = (Button) findViewById(R.id.button);
         hideList = (Button) findViewById(R.id.button2);
         seekBar = (SeekBar) findViewById(R.id.seekBar);
         listView = (ListView) findViewById(R.id.listView);
         textView = (TextView) findViewById(R.id.textView);
 
         namesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
         listView.setAdapter(namesAdapter);
         progress = seekBar.getProgress();
         textView.setText(SPEED + progress);
         handlerText = new Handler() {
             public void handleMessage(Message msg) {
                 textView.setText(SPEED + msg.what);
             }
         };
         handlerList = new Handler() {
             public void handleMessage(Message msg) {
                 namesAdapter.insert(namesList.get(msg.what), msg.what);
                 namesAdapter.notifyDataSetChanged();
             }
         };
         seekBar.setMax(MAX_SEEK_BAR_VALUE);
         seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                 progress = i;
                 new Thread(new Runnable() {
                     @Override
                     public void run() {
                         handlerText.sendEmptyMessage(progress);
                     }
                 }).start();
             }
 
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {
 
             }
 
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {
 
             }
         });
         showList.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 progress = seekBar.getProgress();
                 if (!isRun) {
                     isRun = true;
                     listThread = new Thread(new Runnable() {
                         @Override
                         public void run() {
                             while (counter < namesList.size()) {
                                if (progress > 0) {
                                     try {
                                         Thread.sleep(1000 / progress);
                                     } catch (InterruptedException e) {
                                         e.printStackTrace();
                                     }
                                     handlerList.sendEmptyMessage(counter);
                                     counter++;
                                 }
                                 else if (progress == -1){
                                     break;
                                 }
                             }
                             isRun = false;
                         }
                     });
                     listThread.start();
                 }
             }
         });
 
         hideList.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 namesAdapter.clear();
                 namesAdapter.notifyDataSetChanged();
                 counter = 0;
             }
         });
         hideList.setOnLongClickListener(new View.OnLongClickListener() {
             @Override
             public boolean onLongClick(View view) {
                 progress = -1;
                 namesAdapter.clear();
                 namesAdapter.notifyDataSetChanged();
                 counter = 0;
                 return false;
             }
         });
     }
     @Override
     protected void onStop() {
         progress = -1;
         super.onStop();
     }
 
     @Override
     protected void onDestroy() {
         progress = -1;
         super.onDestroy();
     }
 }
