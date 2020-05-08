 package neto.viajante;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 /** Classe principal do programa nela é possível adicionar novas viages, consultar, editar e excluir
  * 
  * @author n3t0
  *
  */
 public class Lista extends ListActivity {
 
 	static final int ACTIVITY_CREATE = 0;
 	static final int ACTIVITY_EDIT = 1;
 
 	private static final int INSERT_ID = Menu.FIRST;
 	private static final int DELETE_ID = Menu.FIRST + 1;
 	private static final int SOBRE = 4;
 	private static final int MANUAL = 5;
 	private static final String LISTA = "lista";
 	private TravellerDbAdapter mDbHelper;
 	private final int TELA1 = 5;
 	private final int TELA2 = 6;
 	private final int TELA2_DISABLE_BUTTON = 69;
 	private final int TELA3 = 7;
 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main);
 				
 
 		
 		mDbHelper = new TravellerDbAdapter(this);//instancia do banco
 		mDbHelper.open();//abrindo o banco
 		fillData();
 		registerForContextMenu(getListView());
 
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {// ação do botão adicionar viagem
 		switch (item.getItemId()) {
 		case INSERT_ID:
 			criaViagem();
 			return true;
 		case SOBRE:
 			setSobre();
 			break;
 		}
 
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {// cria a opÃ§Ã£o viagem do menu
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, INSERT_ID, 0, R.string.menu_inserir).setIcon(R.drawable.add);
 		menu.add(0, MANUAL, 0, R.string.manual_titulo).setIcon(R.drawable.ajuda);
 		menu.add(0, SOBRE, 0, R.string.menu_sobre).setIcon(R.drawable.sobre);
 		return true;
 
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,// cria o menu deleta a viagem
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, DELETE_ID, 0, R.string.menu_deletar);
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case INSERT_ID:
 			criaViagem();
 			return true;
 		case MANUAL://chama a tela de manual
 			Intent i = new Intent(this, Manual.class);
 			startActivity(i);
 			return true;
 		}
 		
 	
 
 		return super.onOptionsItemSelected(item);
 
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {// opÃ§Ã£o que deleta a
 		// viagem
 		switch (item.getItemId()) {
 		case DELETE_ID:
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 					.getMenuInfo();
 			mDbHelper.deleteNote(info.id);// passa o id do intem pressionado pra
 			// ser deletado
 			fillData();
 			return true;
 		}
 		return super.onContextItemSelected(item);
 
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		//setAds();
 		Log.e(LISTA, "resume");
 		
 	}
 
 	private void criaViagem() {
 		Intent i = new Intent(this, Tela1.class);
 		startActivityForResult(i, ACTIVITY_CREATE);
 	}
 
 	/** Pega dados do banco de dados para exibi-los numa lista de itens
 	 * 
 	 */
 	private void fillData() {
 		// Get all of the notes from the database and create the item list
 		Cursor viagensCursor;
 		viagensCursor = mDbHelper.fetchAllTrips();
 		startManagingCursor(viagensCursor);
 
 		String[] from = new String[] {TravellerDbAdapter.KEY_ORIGEM, TravellerDbAdapter.KEY_DESTINO,
 				TravellerDbAdapter.KEY_DIA_SAIDA, TravellerDbAdapter.KEY_MES_SAIDA, TravellerDbAdapter.KEY_ANO_SAIDA, TravellerDbAdapter.KEY_HSAIDA};//PEGA NO NOME DA TABELA NO CASO DESTINO
 		int[] to = new int[] { R.id.origem, R.id.destino, R.id.dia, R.id.mes, R.id.ano, R.id.hora };
 		
 		
 		// Now create an array adapter and set it to display using our row
 		SimpleCursorAdapter viagens = new SimpleCursorAdapter(this,
 				R.layout.notes_row, viagensCursor, from, to);
 		setListAdapter(viagens);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {// ação que edita quando clica no nome
 		// da viagem
 		
 		super.onListItemClick(l, v, position, id);
 		
 		Cursor viagensCursor;//cursor usado pra pegar valores do banco de dados
 		viagensCursor = mDbHelper.fetchNote(id);
 		startManagingCursor(viagensCursor);
 		
 		Log.e(LISTA, "valor do id: "+id);
 		
 		//pega o valor do campo escolhe tela no banco de dados de acordo com o que foi clicado(id)
 		int escolha = viagensCursor.getInt(viagensCursor.getColumnIndex(TravellerDbAdapter.KEY_ESCOLHETELA));
 		Log.e(LISTA, "Valor do key escolha: "+escolha);
 		
 		switch(escolha){
 		
 		case TELA1:
 			Intent a = new Intent(this, Tela1.class);
 			a.putExtra(TravellerDbAdapter.KEY_ROWID, id);
 			startActivityForResult(a, ACTIVITY_EDIT);
 			Log.e(LISTA, "valor do id na tela1: "+id);
 			break;
 		
 		case TELA3:
 			Intent d = new Intent(this, Tela3.class);
 			d.putExtra(TravellerDbAdapter.KEY_ROWID, id);
 			startActivityForResult(d, ACTIVITY_EDIT);
 			break;
 		}
 		
		
		if (escolha == TELA2 || escolha == TELA2_DISABLE_BUTTON || escolha == 0){
 			Intent c = new Intent(this, Tela2.class);
 			c.putExtra(TravellerDbAdapter.KEY_ROWID, id);
 			startActivityForResult(c, ACTIVITY_EDIT);
 			Log.e(LISTA, "valor do id na tela2: "+escolha);
 		}
 		
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent) {
 		super.onActivityResult(requestCode, resultCode, intent);
 		fillData();
 	}
 	
 	protected void setSobre(){
 		AlertDialog.Builder alerta = new AlertDialog.Builder(Lista.this);
 		alerta.setTitle("Sobre Viajante");
 		alerta.setIcon(R.drawable.icon);
 		alerta.setMessage(
 				"Versão 1.2\n\n"
 				+"Feito por Desílio do C. L. Neto\n"
 				+"Contato: desilio@gmail.com\n"
 				+"Twitter: @netolobo");
 		alerta.show();
 	}
 	
 
 	
 }
