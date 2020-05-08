 package com.ad.cow;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Главная страница
  * 
  */
 public class HomeActivity extends AbstractActivity {
 	/**
 	 * Необходимые переменные
 	 */
 	private GlobalVar gv;
 	private ProgressBar mProgress;
 	private TextView textView;
 	private FeedCountDownTimer countDownTimer;
 	private Toast toast;
 
 	private final float perSecond = 0.001383333f;
 	private final float percentByFood = 1.2f;
 	private final long interval = 1000;
 
 	private float percent;
 	private long time;
 	private float exp;
 	
 	/**
 	 * Старт активности
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		loadPreferences();
 	}
 	
 	/**
 	 * Загружаем настройки и проводим необходимые вычисления
 	 */
 	private void loadPreferences() {
 		long currentTime = new Date().getTime();
 
 		textView = (TextView) findViewById(R.id.textView1);
 
 		// Достаем сохраненные данные
 		gv = GlobalVar.getInstance();
 		percent = gv.getPercent();
 		time = gv.getTime();
 		exp = gv.getExp();
 
 		// Проводим вычисления процента
 		long diff = currentTime - time;
 		float seconds = diff / 1000;
 		float eatenFood = seconds * perSecond;
 
 		float cutPercent = eatenFood / percentByFood;
 		float newPercent = percent - cutPercent;
 		
 
 		percent = Math.max(0, newPercent);
 
 		// Устанавливаем отметку на прогрессбаре голода
 		mProgress = (ProgressBar) findViewById(R.id.progressBar1);
 		mProgress.setProgress((int) percent);
 
 		// Если сыта - живет, иначе смерть
 		if(percent > 0) {
 			Live();
 		} else {
 			Die();
 		}
 	}
 	
 	/**
 	 * Метод жизни коровки
 	 */
 	public void Live() {
 		Alive();
 		
 		// Запускаем таймер голода коровки
 		long timer = (long) ((percent * percentByFood) / perSecond) * 1000;
 		countDownTimer = new FeedCountDownTimer(timer, interval);
 		countDownTimer.start();
 
 		// Создаем всплывающее сообщение для дальнейшего использования
 		// о том что коровка сыта
 		toast = Toast.makeText(this, R.string.cowfeed, Toast.LENGTH_LONG);
 	}
 	
 	/**
 	 * Метод оживления коровки
 	 */
 	public void Alive() {
 		// Изображение живой коровки
 		ImageView cow = (ImageView) findViewById(R.id.imageView1);
 		cow.setImageResource(R.drawable.cow);
 		
 		// Меняем название кнопки на Покормить
 		Button resurrect = (Button) findViewById(R.id.button1);
 		resurrect.setText(R.string.feed);
 		resurrect.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Feed();
 			}
 		});
		
		// Если коровка была мертва то она голодна - кормим
		if(percent <= 0) 
			percent = 5;
 	}
 	
 	/**
 	 * Метод смерти коровки
 	 */
 	public void Die() {
 		// Надпись коровка погибла
 		textView.setText(R.string.cowdie);
 
 		// Изображение мертвой коровки
 		ImageView cow = (ImageView) findViewById(R.id.imageView1);
 		cow.setImageResource(R.drawable.cowdie);
 
 		// Меняем название кнопки на Воскресить
 		Button resurrect = (Button) findViewById(R.id.button1);
 		resurrect.setText(R.string.resurrect);
 		resurrect.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Live();
 			}
 		});
 	}
 	
 	/**
 	 * Метод выполняющийся при нажатии кнопки Покормить
 	 */
 	public void Feed() {
 		/*
 		 * Показываем ачивку Intent intent = new Intent(this,
 		 * AchieveActivity.class);
 		 * intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		 * startActivity(intent);
 		 */
 
 		// Вычисляем новое значение процента голода и добавляем опыт
 		int newPercent = mProgress.getProgress() + 10;
 		if(newPercent < 100){
 			exp += 15;
 		}	
 		
 		if (newPercent <= 100) {
 			percent += 10;
 		} else {
 			newPercent = 100;
 			percent = 100;
 		}
 		mProgress.setProgress(newPercent);
 
 		// Останавливаем предыдущий таймер и стартуем с новыми данными
 		countDownTimer.cancel();
 		long timer = (long) ((percent * percentByFood) / perSecond) * 1000;
 		countDownTimer = new FeedCountDownTimer(timer, interval);
 		countDownTimer.start();
 
 		// Если процент голода больше 50 выводим всплывающее сообщение
 		// о том что коровка сыта
 		if (newPercent > 50) {
 			toast.cancel();
 			toast.show();
 		}
 	}
 	
 	/**
 	 * Класс таймера обратного отсчета голода коровки
 	 * 
 	 */
 	private class FeedCountDownTimer extends CountDownTimer {
 		public FeedCountDownTimer(long startTime, long interval) {
 			super(startTime, interval);
 		}
 
 		/**
 		 * При истечении времени коровка погибает
 		 */
 		@Override
 		public void onFinish() {
 			Die();
 		}
 
 		/**
 		 * Выполняет при каждом наступлении интервала
 		 */
 		@Override
 		public void onTick(long millisUntilFinished) {
 			percent = (millisUntilFinished / 1000) * perSecond / percentByFood;
 
 			// Устанавливаем новый процент голода коровки
 			int newPercent = (int) Math.round(percent);
 			mProgress.setProgress(newPercent);
 
 			// Задаем формат для оставшегося времени
 			SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
 			Date resultdate = new Date(millisUntilFinished);
 
 			// Узнаем общшее количество оставшихся часов
 			long hours = millisUntilFinished / 1000 / 60 / 60;
 			textView.setText(getString(R.string.goback) + ": " + hours + ":"
 					+ sdf.format(resultdate));
 		}
 	}
 	
 	/**
 	 * При завершении экшена сохраняем данные
 	 */
 	@Override
 	protected void onPause() {		
 		gv.setPercent(percent);
 		gv.setTime(new Date().getTime());
 		gv.setExp(exp);
 		gv.save();
 		
 		super.onPause();
 	}
 
 }
