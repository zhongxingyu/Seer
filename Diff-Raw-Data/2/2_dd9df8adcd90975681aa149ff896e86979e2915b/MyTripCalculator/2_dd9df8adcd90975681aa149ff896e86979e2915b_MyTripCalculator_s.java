 package com.android.forb.app;
 
 import static android.graphics.Color.rgb;
 import static com.android.forb.util.NumberUtil.isZeroOrNull;
 import static com.android.forb.util.NumberUtil.round;
 import static com.android.forb.util.NumberUtil.toDouble;
 import static com.android.forb.util.StringUtil.isBlank;
 
 import java.util.Random;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.android.forb.util.AppUtil;
 import com.android.forb.util.ToastUtil;
 
 public class MyTripCalculator extends Activity implements View.OnClickListener {
 
 	private static final int MAX_NUM_ITENS = 7;
 	private Button buttonCalcular;
 	private TextView tvResultado;
 	private EditText etKm;
 	private EditText etKmL;
 	private EditText etValorCombustivel;
 	private TextView tvResultadoPorPassageiro;
 	private EditText etNumeroPassageiros;
 	private View buttonAddItem;
 	private static int idCounter = 0;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		init();
 	}
 
 	private void init() {
 		tvResultado = (TextView) findViewById(R.id.tvResultado);
 		tvResultadoPorPassageiro = (TextView) findViewById(R.id.tvResultadoPorPassageiro);
 
 		etKm = (EditText) findViewById(R.id.etKm);
 		etKmL = (EditText) findViewById(R.id.etKmL);
 		etValorCombustivel = (EditText) findViewById(R.id.etValorCombustivel);
 		etNumeroPassageiros = (EditText) findViewById(R.id.etNumeroPassageiros);
 
 		buttonCalcular = (Button) findViewById(R.id.btCalcular);
 		buttonCalcular.setOnClickListener(MyTripCalculator.this);
 
 		buttonAddItem = (Button) findViewById(R.id.btAddItem);
 		buttonAddItem.setOnClickListener(MyTripCalculator.this);
 	}
 
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.btCalcular:
 			if (isValidState()) {
 				doCalcular();
 			}
 			break;
 
 		case R.id.btAddItem:
 			doAddItem();
 			break;
 		}
 	}
 
 	private void doCalcular() {
 		tvResultado.setText("Total da despesa: " + calcular());
 		final Random rd = new Random();
 		tvResultado.setTextSize(25);
 		tvResultado.setTextColor(rgb(rd.nextInt(265), rd.nextInt(265),
 				rd.nextInt(265)));
 		tvResultadoPorPassageiro.setText("Despesa por passageiro: "
 				+ dividirPorNumeroPassageiros());
 
 		AppUtil.hideSwKeyBoard(etNumeroPassageiros, MyTripCalculator.this);
 	}
 
 	private void doAddItem() {
 		if (idCounter == MAX_NUM_ITENS) {
 			ToastUtil.show(MyTripCalculator.this,
 					"Não é permitido inserir mais itens.");
 			return;
 		}
 
 		idCounter++;
 
 		final LinearLayout mainLinearLayout = (LinearLayout) findViewById(R.id.myLinearLayout);
 
 		final LinearLayout externLinerLayout = new LinearLayout(this);
 		externLinerLayout.setWeightSum(100);
		externLinerLayout.setOrientation(LinearLayout.VERTICAL);
 		
 		final LinearLayout textLinearLayout = new LinearLayout(this);
 		textLinearLayout.setId(100 + idCounter);
 		textLinearLayout.setWeightSum(80);
 		textLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
 		final EditText editText = new EditText(this);
 		editText.setId(idCounter);
 		editText.setHint("Exemplo: 30.50 (Pneu furado)");
 		editText.setFocusable(true);
 		editText.setFocusableInTouchMode(true);
 		editText.setInputType(InputType.TYPE_CLASS_NUMBER);
 		editText.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 		textLinearLayout.addView(editText);
 		externLinerLayout.addView(textLinearLayout);
 		
 		
 		final LinearLayout buttonLinearLayout = new LinearLayout(this);
 		buttonLinearLayout.setId(100 + idCounter);
 		buttonLinearLayout.setWeightSum(20);
 		buttonLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
 		final Button button = new Button(this);
 		button.setId(10 + idCounter);
 		button.setText("X");
 		button.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 		button.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (idCounter == 0) {
 					return;
 				}
 
 				final LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.myLinearLayout);
 				mLinearLayout.removeView(findViewById(idCounter));
 				mLinearLayout.removeView(v);
 				idCounter--;
 			}
 		});
 		
 		buttonLinearLayout.addView(button);
 		externLinerLayout.addView(buttonLinearLayout);
 		mainLinearLayout.addView(externLinerLayout);
 	}
 
 	private boolean isValidState() {
 		if (isBlankFields()) {
 			ToastUtil.show(MyTripCalculator.this,
 					"Por favor preencha todos os campos.");
 
 			return false;
 		}
 
 		if (isInvalidFields()) {
 			ToastUtil.show(MyTripCalculator.this,
 					"Não é permitido o valor 0 (zero) ou nulo.");
 
 			return false;
 		}
 
 		return true;
 	}
 
 	private boolean isBlankFields() {
 		return isBlank(etKmL) || isBlank(etKm) || isBlank(etNumeroPassageiros)
 				|| isBlank(etValorCombustivel);
 	}
 
 	private boolean isInvalidFields() {
 		try {
 			Double double1 = toDouble(etKmL);
 			Double double2 = toDouble(etKm);
 			Double double3 = toDouble(etNumeroPassageiros);
 			Double double4 = toDouble(etValorCombustivel);
 			return isZeroOrNull(double1) || isZeroOrNull(double2)
 					|| isZeroOrNull(double3) || isZeroOrNull(double4);
 
 		} catch (IllegalArgumentException e) {
 			return false;
 		}
 
 	}
 
 	private Double calcular() {
 		return round(toDouble(etKm) / toDouble(etKmL)
 				* toDouble(etValorCombustivel));
 	}
 
 	private Double dividirPorNumeroPassageiros() {
 		return round(calcular() / toDouble(etNumeroPassageiros));
 	}
 
 }
