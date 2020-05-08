 package iu.texto.enums;
 
 public enum Mensagem {
 
 	SUCESSO("Sucesso!"),
 	SUCESSO_CONEXAO("Conexo efetuada com sucesso!"),
 	SUCESSO_NOVO_STATUS("Seu status foi atualizado!"),
 	SUCESSO_POST_MURAL("Sua mensagem foi postada no mural de #nome!"),
 	
 	ERRO("Ocorreu um erro!"),
 	ERRO_CONEXAO("A Conexo falhou!"),
 	ERRO_OPCAO_INVALIDA("No existe opo de menu com o nmero digitado."),
 	ERRO_NOVO_STATUS("Que pena! Houve uma falha e seu status no foi atualizado!"),
 	ERRO_POST_MURAL("Que pena! Houve uma falha e sua mensagem no foi postada no mural!"),
 	ERRO_FALHA_BROWSE("Falha ao abrir a URL no navegador padro."),
 	ERRO_ATUALIZACOES("Falha ao buscar as atualizaes."),
 	ERRO_PESQUISA_AMIGO("Amigo no encontrado."),
 	
 	MSG_DIGITE_OPCAO("  Digite sua opo e aperte [Enter]: "),
 	MSG_URL_NAVEGADOR("Acesse este endereo atravs do seu navegador de internet: "),
 	MSG_ESCREVA_CODIGO_MANUAL("Um novo endereo (URL) foi gerado na barra de endereos do seu navegador.\nCole-o aqui e aperte [Enter]: "),
 	MSG_ESCREVA_CODIGO("Cole aqui o endereo (URL) da pgina que foi aberta em seu navegador e aperte [Enter]: "),
 	MSG_NOVO_STATUS("- ATUALIZACAO DE STATUS -\n\nNo que voc est pensando "),
 	MSG_POST_MURAL("- MURAL DE #nome -\n\nQual a mensagem que voc quer postar? "),
 	MSG_PESQUISA_AMIGO("Qual o nome, ou parte do nome, que voc quer procurar entre seus amigos? "),
 	MSG_SELECIONA_AMIGO("Digite 0 para voltar ao Menu ou o ID de um amigo da lista para selecion-lo: "),
 	MSG_TECLA_CONTINUAR("Digite qualquer tecla e aperte [Enter] para continuar."),
 	
 	MENU_INICIAL("MENU INICIAL"),
 	MENU_PRINCIPAL("O QUE DESEJA FAZER?"),
 	MENU_AMIGOS("LISTA DE AMIGOS");
 	
 	private String texto;
 	
 	private Mensagem(String texto){
 		this.texto = texto;
 	}
 
 	public String getTexto() {
 		return texto;
 	}
 
 }
