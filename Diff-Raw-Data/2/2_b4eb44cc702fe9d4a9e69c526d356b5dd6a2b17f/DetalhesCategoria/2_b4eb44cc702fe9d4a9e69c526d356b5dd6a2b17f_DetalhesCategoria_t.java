 package info.diegoramos.alergias;
 
 import info.diegoramos.alergias.R;
 import info.diegoramos.alergias.Utils.ToastManager;
 import info.diegoramos.alergias.Utils.validacoes;
 import info.diegoramos.alergias.entity.Categoria;
 import info.diegoramos.alergias.persistence.DAOCategoria;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 
 /**
  * 
  * @author Diego Ramos <rdiego26@gmail>
  *
  */
 public class DetalhesCategoria extends Activity
 {
 	Categoria c;
 	DAOCategoria daoC;
 	
 	//Componentes da tela
 	EditText edtNome;
 
 	//Mensagens
 	String msg_duplicidade_categoria;
 	String msg_sucesso_gravacao;
 	String msg_falha_gravacao;	
 	String msg_sucesso_alteracao;
 	String msg_falha_alteracao;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.detalhes_categoria);
 		
 		edtNome = (EditText) findViewById(R.detalhes_categoria.txtNomeCategoria);
 		
 		loadContent();
 		
 	}	
     
   /**
    * Responsável por obter um objeto Categoria serializado e setar os valores na tela
    */
     private void loadContent() {
 		//obtain client via Extra
 		Bundle extras = getIntent().getExtras();
 		c = (Categoria) extras.getSerializable("_object_categoria");
 		
 		if ( c != null) {
 			edtNome.setText(c.getNome());
 		}    	
     }
     
     /**
      * Responsável por efetuar a atualização do objeto Categoria
      * @param View ( ListarCategoria )
      */    
     public void update(View vw) {
     	daoC = DAOCategoria.getInstance(this);
     	
     	c.setNome(edtNome.getText().toString());
     	
 		msg_duplicidade_categoria = this.getString(R.string.lbl_erro_duplicidade_categoria);
 		msg_sucesso_gravacao = this.getString(R.string.lbl_sucesso_cadastro_categoria);
 		msg_falha_gravacao = this.getString(R.string.lbl_falha_cadastro_categoria);	
 		msg_sucesso_alteracao = this.getString(R.string.lbl_sucesso_alteracao_categoria);
 		msg_falha_alteracao = this.getString(R.string.lbl_falha_alteracao_categoria);
 		
 		//Validação dos campos
 		validacoes validate = new validacoes();				
 		boolean aux1;
 		
 		aux1 = validate.isNull("Categoria ", c.getNome(), 1, getApplicationContext());
 		if(aux1 != true)
 		{
 			//Verifica duplicidade
 			if(daoC.buscarNome(c) != null)
 			{
 				ToastManager.show(getApplicationContext(), msg_duplicidade_categoria, 2);
 			}
 			else
 			{
				if(daoC.update(c) != -1) //Salva para o banco de dados o objeto povoado	
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
