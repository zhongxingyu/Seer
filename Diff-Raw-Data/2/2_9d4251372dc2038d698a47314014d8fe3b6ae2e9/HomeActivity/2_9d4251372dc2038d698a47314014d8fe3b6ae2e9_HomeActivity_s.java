 package com.ad.cow;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
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
 	private final String MY_PREFS = "MY_PREFS";
 
 	private ProgressBar mProgress;
 	private TextView textView;
 	private SharedPreferences mySharedPreferences;
 	private FeedCountDownTimer countDownTimer;
 	private Toast toast;
 
 	private final float perSecond = 0.001383333f;
 	private final float percentByFood = 1.2f;
 	private final long interval = 1000;
 
 	private float percent;
 	private long time;
 
 	/**
 	 * Старт активности
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		loadPreferences();
 	}
 
 	/**
 	 * Загружаем настройки и проводим необходимые вычисления
 	 */
 	private void loadPreferences() {
 		int mode = Activity.MODE_MULTI_PROCESS;
 		long currentTime = new Date().getTime();
 
 		textView = (TextView) findViewById(R.id.textView1);
 
 		// Достаем сохраненные данные
 		mySharedPreferences = getSharedPreferences(MY_PREFS,mode);
 		percent = mySharedPreferences.getFloat("percentf", 10.0f);
 		time = mySharedPreferences.getLong("time", currentTime);
 
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
 	private void Live() {
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
 	private void Alive() {
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
 	}
 	
 	/**
 	 * Метод смерти коровки
 	 */
 	private void Die() {
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
				Alive();
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
 
 		// Вычисляем новое значение процента голода
 		int newPercent = mProgress.getProgress() + 10;
 
 		if (newPercent <= 100) {
 			percent += 10;
 		} else {
 			newPercent = 100;
 			percent = 100;
 		}
 		mProgress.setProgress(newPercent);
 
 		// Сохраняем измененные данные
 		SharedPreferences.Editor editor = mySharedPreferences.edit();
 		editor.putFloat("percentf", percent);
 		editor.putLong("time", new Date().getTime());
 		editor.commit();
 
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
 	protected void onStop() {
 		super.onStop();
 
 		SharedPreferences.Editor editor = mySharedPreferences.edit();
 		editor.putFloat("percentf", percent);
 		editor.putLong("time", new Date().getTime());
 		editor.commit();
 	}
 
 }
