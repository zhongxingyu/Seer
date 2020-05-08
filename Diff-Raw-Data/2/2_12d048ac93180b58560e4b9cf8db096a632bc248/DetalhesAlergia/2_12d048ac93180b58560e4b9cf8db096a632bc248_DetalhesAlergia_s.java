 package info.diegoramos.alergias;
 
 import info.diegoramos.alergias.R;
 import info.diegoramos.alergias.Utils.ToastManager;
 import info.diegoramos.alergias.Utils.validacoes;
 import info.diegoramos.alergias.componentes.CategoriaSpinnerAdapter;
 import info.diegoramos.alergias.entity.Alergia;
 import info.diegoramos.alergias.entity.Categoria;
 import info.diegoramos.alergias.persistence.DAOAlergia;
 import info.diegoramos.alergias.persistence.DAOCategoria;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 /**
  * 
  * @author Diego Ramos <rdiego26@gmail>
  *
  */
 public class DetalhesAlergia extends Activity{
 
 	DAOAlergia daoA;
 	DAOCategoria daoC;
 	Alergia a;
 	
 	//Componentes da tela
 	EditText edtNome, edtObs;
 	Spinner spiCategoria;
 	
 	
 	//Mensagens
 	String msg_duplicidade_alergia;
 	String msg_sucesso_gravacao;
 	String msg_falha_gravacao;	
 	String msg_sucesso_alteracao;
 	String msg_falha_alteracao;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.detalhes_alergia);
 	    
 		//Setando Mensagens
 		msg_duplicidade_alergia = getString(R.string.lbl_erro_duplicidade_alergia);
 		msg_sucesso_gravacao = getString(R.string.lbl_sucesso_cadastro_alergia);
 		msg_falha_gravacao = getString(R.string.lbl_falha_cadastro_alergia);	
 		msg_sucesso_alteracao = getString(R.string.lbl_sucesso_alteracao_alergia);
 		msg_falha_alteracao = getString(R.string.lbl_falha_alteracao_alergia);
 	    
 	    
 		//Componentes
 		spiCategoria = (Spinner)findViewById(R.detalhes_alergia.sp_categoria);
 		edtNome = (EditText)findViewById(R.detalhes_alergia.txtNomeAlergia);
 		edtObs = (EditText)findViewById(R.detalhes_alergia.txtObsAlergia);
 	    
 		loadCategoriaSpinner();
 		loadContent();
 		
 	}
     
     /**
      * Responsável por obter um objeto Categoria serializado e setar os valores na tela
      */    
     private void loadContent() {
 		//obtain client via Extra
 		Bundle extras = getIntent().getExtras();
 		a = (Alergia) extras.getSerializable("_object_alergia");
 		
 		if ( a != null) {
 			daoC = DAOCategoria.getInstance(this);
 			
 			setCategoriaSpinner( daoC.getById(a.getId_categoria()));
 			edtNome.setText(a.getNome());
 			edtObs.setText(a.getObs());
 		}     	
     }
     
     /**
      * Responsável por preencher o Spinner de Categoria
      */
     private void loadCategoriaSpinner() {
     	spiCategoria.setAdapter(CategoriaSpinnerAdapter.getAdapter(this));
     }
     /**
      * Recebe um objeto Categoria e seta este no Spinner Categoria
      * @param Categoria
      */
     
 	private void setCategoriaSpinner(Categoria cat) {
 		for (int i = 0; i < spiCategoria.getCount(); i++) {  
             if (spiCategoria.getItemAtPosition(i).toString()
             		.replace("{nomeCategoria=", "").replace("}", "")
             			.equals(cat.getNome())) {  
             	spiCategoria.setSelection(i);  
             }  
         }  
 	}
 
 	/**
 	 * Responsável por obter o objeto selecionado no Spinner Categoria
 	 */
 	private Categoria getCategoriaSpinner() {
 		Categoria categoria = daoC.getByName( spiCategoria.getSelectedItem().toString().replace("{nomeCategoria=", "").replace("}", "") );
 		
 		return categoria;
 	}
 
 	
 	/**
     * Responsável por efetuar a atualização do objeto Alergia
     * @param View ( ListarAlergia )
     */    	
 	public void update(View vw) {
 		daoA = DAOAlergia.getInstance(this);
 		
 		a.setId_categoria( daoC.getByName(getCategoriaSpinner().getNome()).getId_categoria() );
 		a.setNome(edtNome.getText().toString());
 		a.setObs(edtObs.getText().toString());
 		
 		
 		validacoes validate = new validacoes();				
 		boolean aux1;
 		
 		aux1 = validate.isNull("Alergia ", a.getNome(), 1, getApplicationContext());		
 		
 		
 		String msg_duplicidade_alergia = getString(R.string.lbl_erro_duplicidade_alergia);
 		String msg_sucesso_gravacao = getString(R.string.lbl_sucesso_cadastro_alergia);
 		String msg_falha_gravacao = getString(R.string.lbl_falha_cadastro_alergia);
 		
 		if(aux1 != true)
 		{
 			//Verifica duplicidade
 			if(daoA.buscarNomeDuplicado(a) != null)
 			{
 				ToastManager.show(getApplicationContext(), msg_duplicidade_alergia, 1);
 			}
 			else
 			{
				if(daoA.save(a) != -1) //Salva para o banco de dados o objeto povoado	
 				{
 					ToastManager.show(getApplicationContext(), msg_sucesso_gravacao, 0);
 					finish(); //sae da tela						
 				}
 				else
 				{
 					ToastManager.show(getApplicationContext(), msg_falha_gravacao, 1);
 				}
 			}
 		}		
 		
 		
 	}
 	
 }
