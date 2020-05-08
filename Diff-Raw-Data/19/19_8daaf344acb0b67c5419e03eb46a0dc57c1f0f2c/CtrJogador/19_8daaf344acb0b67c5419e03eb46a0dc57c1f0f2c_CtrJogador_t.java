 package sgcmf.control;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import org.hibernate.HibernateException;
 import org.hibernate.Transaction;
 import sgcmf.model.dao.GeneralDAO;
 import sgcmf.model.dao.JogadorDAO;
 import sgcmf.model.hibernate.Jogador;
 import sgcmf.model.hibernate.Selecao;
 import sgcmf.model.hibernate.Usuario;
 import sgcmf.model.other.ResultadoOperacao;
 import sgcmf.model.other.TipoResultadoOperacao;
 
 public class CtrJogador
 {
     public Jogador carregaJogadorById(GeneralDAO gdao, Short idJogador)
     {
         Jogador jogador;
 
         jogador = new Jogador();
         gdao.carregar(jogador, idJogador);
 
         return jogador;
     }
 
     public String[][] queryJogadoresEmCampo(Short idJogo)
     {
         JogadorDAO jDAO;
         String[][] dadosJogadores;
         ArrayList alJogador;
 
         jDAO = new JogadorDAO();
         alJogador = jDAO.queryJogadoresEmCampo(idJogo);
         dadosJogadores = arrayList2StringMatrix(alJogador);
         jDAO.fecharSessao();
 
         return dadosJogadores;
     }
 
     public String[][] queryJogadoresEmCampoByNome(Short idJogo, String nome)
     {
         JogadorDAO jDAO;
         String[][] dadosJogadores;
         ArrayList alJogador;
 
         jDAO = new JogadorDAO();
         alJogador = jDAO.queryJogadoresEmCampoByNome(idJogo, nome);
         dadosJogadores = arrayList2StringMatrix(alJogador);
         jDAO.fecharSessao();
 
         return dadosJogadores;
     }
 
     public String[][] queryOutrosJogadoresEmCampoSelecao(Short idJogo, Short idSelecao, Short idJogador)
     {
         JogadorDAO jDAO;
         String[][] dadosJogadores;
         ArrayList alJogador;
 
         jDAO = new JogadorDAO();
         alJogador = jDAO.queryOutrosJogadoresEmCampoSelecao(idJogo, idSelecao, idJogador);
         dadosJogadores = arrayList2StringMatrix(alJogador);
         jDAO.fecharSessao();
 
         return dadosJogadores;
     }
 
     public String[][] queryOutrosJogadoresEmCampoSelecaoByNome(Short idJogo, Short idSelecao, Short idJogador, String nome)
     {
         JogadorDAO jDAO;
         String[][] dadosJogadores;
         ArrayList alJogador;
 
         jDAO = new JogadorDAO();
         alJogador = jDAO.queryOutrosJogadoresEmCampoSelecaoByNome(idJogo, idSelecao, idJogador, nome);
         dadosJogadores = arrayList2StringMatrix(alJogador);
         jDAO.fecharSessao();
 
         return dadosJogadores;
     }
 
     public String[][] queryReservasSelecao(Short idJogo, Short idSelecao)
     {
         JogadorDAO jDAO;
         String[][] dadosJogadores;
         ArrayList alJogador;
 
         jDAO = new JogadorDAO();
         alJogador = jDAO.queryReservasSelecao(idJogo, idSelecao);
         dadosJogadores = arrayList2StringMatrix(alJogador);
         jDAO.fecharSessao();
 
         return dadosJogadores;
     }
 
     public String[][] queryReservasSelecaoByNome(Short idJogo, Short idSelecao, String nome)
     {
         JogadorDAO jDAO;
         String[][] dadosJogadores;
         ArrayList alJogador;
 
         jDAO = new JogadorDAO();
         alJogador = jDAO.queryReservasSelecaoByNome(idJogo, idSelecao, nome);
         dadosJogadores = arrayList2StringMatrix(alJogador);
         jDAO.fecharSessao();
 
         return dadosJogadores;
     }
 
     public Short queryIdSelecaoJogador(Short idJogador)
     {
         JogadorDAO jDAO;
         jDAO = new JogadorDAO();
 
         return jDAO.queryIdSelecaoJogador(idJogador);
     }
 
     public String[][] queryAllDataJogadorTecnico(Usuario u)
     {
         JogadorDAO jDAO;
         String[][] dadosJogadores;
         ArrayList alJogador;
         Selecao s;
         jDAO = new JogadorDAO();
         Iterator iterator = u.getSelecaos().iterator();
 
         s = (Selecao) iterator.next();
         alJogador = jDAO.listaTodosBySelecao(s.getId());
         dadosJogadores = arrayList2StringMatrixFull(alJogador);
         jDAO.fecharSessao();
 
         return dadosJogadores;
     }
 
     public String[][] queryAllDataJogadorByNomeAndByUser(String nome, Usuario u)
     {
         JogadorDAO jDAO;
         String[][] dadosJogador;
         ArrayList<Jogador> alJogador;
         Iterator iterator = u.getSelecaos().iterator();
         Selecao s;
         s = (Selecao) iterator.next();
         jDAO = new JogadorDAO();
         alJogador = jDAO.queryJogadorByNomeAndByUser(nome, s.getId());
         dadosJogador = arrayList2StringMatrixFull(alJogador);
         jDAO.fecharSessao();
 
         return dadosJogador;
     }
 
     public String[][] queryAllDataJogadorByPosicaoAndByUser(String posicao, Usuario u)
     {
         JogadorDAO jDAO;
         String[][] dadosJogador;
         ArrayList<Jogador> alJogador;
         Iterator iterator = u.getSelecaos().iterator();
         Selecao s;
         s = (Selecao) iterator.next();
 
         jDAO = new JogadorDAO();
         alJogador = jDAO.queryJogadorByPosicaoAndByUser(posicao, s.getId());
         dadosJogador = arrayList2StringMatrixFull(alJogador);
         jDAO.fecharSessao();
 
         return dadosJogador;
     }
 
     private String[][] arrayList2StringMatrix(ArrayList<Jogador> alJogador)
     {
         String[][] dadosJogadores;
         Jogador j;
 
         dadosJogadores = new String[alJogador.size()][5];
         for (int i = 0; i < alJogador.size(); i++)
         {
             j = alJogador.get(i);
             dadosJogadores[i][0] = String.valueOf(j.getId());
             dadosJogadores[i][1] = j.getSelecao().getPais();
             dadosJogadores[i][2] = String.valueOf(j.getNcamisa());
             dadosJogadores[i][3] = j.getNome();
             dadosJogadores[i][4] = j.getPosicao();
         }
 
         return dadosJogadores;
     }
 
     private String[][] arrayList2StringMatrixFull(ArrayList<Jogador> alJogador)
     {
         String[][] dadosJogadores;
         Jogador j;
         String titular;
 
         dadosJogadores = new String[alJogador.size()][8];
         for (int i = 0; i < alJogador.size(); i++)
         {
             j = alJogador.get(i);
             dadosJogadores[i][0] = String.valueOf(j.getId());
             dadosJogadores[i][1] = String.valueOf(j.getNcamisa());
             dadosJogadores[i][2] = j.getNome();
             dadosJogadores[i][3] = String.valueOf(j.getDatanasc());
             dadosJogadores[i][4] = String.valueOf(j.getAltura());
             dadosJogadores[i][5] = j.getPosicao();
             dadosJogadores[i][6] = j.getSelecao().getPais();
             if (j.isTitular())
             {
                 titular = "Sim";
             }
             else
             {
                 titular = "Não";
             }
             dadosJogadores[i][7] = titular;
         }
 
         return dadosJogadores;
     }
 
     public ResultadoOperacao cadastrarJogador(String numCamisa, String nome, String dataNascimento,
             String altura, boolean titular, String posicao, Usuario user)
     {
         Short nCamisa;
         Date dtaNascimento;
         BigDecimal aAltura;
         Transaction tr;
         Jogador j = new Jogador();
         ResultadoOperacao result;
         String errorMessege;
         GeneralDAO gdao;
         Iterator iterator = user.getSelecaos().iterator();
         Selecao s;
         s = (Selecao) iterator.next();
         boolean bolGoleiro;
         gdao = new GeneralDAO();
         if (posicao.equals("Goleiro"))
         {
             bolGoleiro = true;
         }
         else
         {
             bolGoleiro = false;
         }
        errorMessege = validaCampos('c', numCamisa, dataNascimento, altura, bolGoleiro, false, s.getId(), posicao);
         if (errorMessege.equals(""))
         {
             nCamisa = Short.parseShort(numCamisa);
             dtaNascimento = new Date(dataNascimento);
             aAltura = new BigDecimal(altura);
             tr = gdao.getSessao().beginTransaction();
             try
             {
                 j.setNcamisa(nCamisa);
                 j.setNome(nome);
                 j.setDatanasc(dtaNascimento);
                 j.setAltura(aAltura);
                 j.setTitular(titular);
                 j.setPosicao(posicao);
                 j.setSelecao(s);
                 gdao.salvar(j);
                 tr.commit();
                 result = new ResultadoOperacao("Jogador Cadastrado com êxito.",
                         TipoResultadoOperacao.EXITO);
             }
             catch (HibernateException he)
             {
                 result = new ResultadoOperacao("Erro no Hibernate.\n" + he.getMessage(),
                         TipoResultadoOperacao.ERRO);
             }
             gdao.fecharSessao();
         }
         else
         {
             result = new ResultadoOperacao("Falha no cadastro de jogador.\n" + errorMessege,
                     TipoResultadoOperacao.ERRO);
         }
         return result;
     }
 
     public ResultadoOperacao alterarJogador(String strIdJogador, String numCamisaNovo,
             String numCamisaAtual, String nome, String dtaNascimento, String altura,
             String posicao, Usuario user)
     {
         Short nCamisa;
         Date dataNascimento;
         BigDecimal pAltura;
         Short shortIdJogador;
         Jogador j = new Jogador();
         Transaction tr;
         GeneralDAO gdao;
         ResultadoOperacao result;
         String errorMessege;
         boolean bolGoleiro;
         boolean bolNumCamisaJogador;
         Iterator iterator;
         iterator = user.getSelecaos().iterator();
         Selecao s = (Selecao) iterator.next();
         shortIdJogador = new Short(strIdJogador);
 
         bolNumCamisaJogador = isNumCamisaVelhaIgualCamisaNova(numCamisaNovo, numCamisaAtual);
         bolGoleiro = isJogadorGoleiro(shortIdJogador);
         errorMessege = validaCampos('a', numCamisaNovo, dtaNascimento, altura,
                bolGoleiro, bolNumCamisaJogador, s.getId(), posicao);
         if (errorMessege.equals(""))
         {
             gdao = new GeneralDAO();
             nCamisa = Short.parseShort(numCamisaNovo);
             dataNascimento = new Date(dtaNascimento);
             pAltura = new BigDecimal(altura);
             tr = gdao.getSessao().beginTransaction();
             gdao.carregar(j, shortIdJogador);
             try
             {
                 j.setNcamisa(nCamisa);
                 j.setNome(nome);
                 j.setDatanasc(dataNascimento);
                 j.setAltura(pAltura);
                 j.setPosicao(posicao);
                 gdao.atualizar(j);
                 tr.commit();
                 result = new ResultadoOperacao("Jogador Alterado com êxito.", TipoResultadoOperacao.EXITO);
             }
             catch (HibernateException he)
             {
                 result = new ResultadoOperacao("Erro no Hibernate.\n" + he.getMessage(), TipoResultadoOperacao.ERRO);
             }
             gdao.fecharSessao();
         }
         else
         {
             result = new ResultadoOperacao("Falha na alteracao do jogador.\n" + errorMessege, TipoResultadoOperacao.ERRO);
         }
         return result;
     }
 
     public ResultadoOperacao removerJogador(String idJogador)
     {
         Jogador j = new Jogador();
         Transaction tr;
         GeneralDAO gDAO;
         ResultadoOperacao resultado;
         try
         {
             gDAO = new GeneralDAO();
             tr = gDAO.getSessao().beginTransaction();
             gDAO.carregar(j, new Short(idJogador));
             gDAO.apagar(j);
             tr.commit();
             resultado = new ResultadoOperacao("Jogador excluido com sucesso.", TipoResultadoOperacao.EXITO);
         }
         catch (HibernateException he)
         {
             resultado = new ResultadoOperacao("Falha na exclusão do jogador.\n" + he.getMessage(),
                     TipoResultadoOperacao.ERRO);
         }
         return resultado;
     }
 
     private String validaCampos(char metodo, String numCamisa, String dataNascimento,
            String altura, boolean bolGoleiro, boolean bolNumCamisaIguais, Short idSelecao, String novaPosicao)
     {
         String errorMessege = "";
         Short camisa;
         Date date;
         BigDecimal aAltura;
         int resultadoCamisaExistente;
 
         try
         {
             camisa = Short.parseShort(numCamisa);
             resultadoCamisaExistente = verificarNumeroCamisaExistente(camisa, idSelecao);
             if (resultadoCamisaExistente != 0 && bolNumCamisaIguais == false)
             {
                 errorMessege = "Já existe um jogador cadastrado com este número de camisa.";
             }
             if (camisa < 1 || camisa > 23)
             {
                 errorMessege = "O valor da camisa deve estar entre 1 e 23.";
                 return errorMessege;
             }
         }
         catch (NumberFormatException nfe)
         {
             errorMessege = "Digite um número valido para camisa.";
         }
         if (errorMessege.equals(""))
         {
             try
             {
                 date = new Date(dataNascimento);
             }
             catch (IllegalArgumentException ilae)
             {
                 errorMessege = "Formato de data é inválido. Deve seguir o padrão AAAA/MM/DD.";
             }
         }
         if (errorMessege.equals(""))
         {
             try
             {
                 aAltura = new BigDecimal(altura);
             }
             catch (NumberFormatException nfe)
             {
                 errorMessege = "Digite um número valido para altura, usando \".\""
                         + " para separar o metro de centímetro.";
             }
         }
         if (errorMessege.equals(""))
         {
             int resultado = qtdeTitularesSelecao(idSelecao);
             if (resultado >= 11)
             {
                 errorMessege = "É permitido apenas 11 jogadores titulares por seleção.";
             }
         }
         if (errorMessege.equals(""))
         {
             int resultado = qtdeGoleirosSelecao(idSelecao);
             if (metodo == 'c' && bolGoleiro == true)
             {
                 if (resultado >= 3)
                 {
                     errorMessege = "É permitido apenas 3 Goleiros por seleção.";
                 }
             }
            else if (metodo == 'a')
             {
                //A merda dessa condição da alteração esta errada.
                 if (bolGoleiro == false)
                 {
                    if (resultado >= 3 && novaPosicao.equals("Goleiro"))
                     {
                         errorMessege = "É permitido apenas 3 Goleiros por seleção.";
                     }
                 }
             }
         }
         return errorMessege;
     }
 
     private int qtdeTitularesSelecao(Short idSelecao)
     {
         JogadorDAO jogadorDAO = new JogadorDAO();
         int qtdeTitulares;
         qtdeTitulares = jogadorDAO.queryQuantidadeJogadorTitularesSelecao(idSelecao);
         jogadorDAO.fecharSessao();
         return qtdeTitulares;
     }
 
     private int qtdeGoleirosSelecao(Short idSelecao)
     {
         JogadorDAO jogadorDAO = new JogadorDAO();
         int qtdeGoleiros;
         qtdeGoleiros = jogadorDAO.queryQuantidadeGoleirosSelecao(idSelecao);
         jogadorDAO.fecharSessao();
         return qtdeGoleiros;
     }
 
     private boolean isJogadorGoleiro(Short idJogador)
     {
         JogadorDAO jogadorDAO = new JogadorDAO();
         String posicao;
         posicao = jogadorDAO.queryPosicaoJogador(idJogador);
         if (posicao.equals("Goleiro"))
         {
             return true;
         }
         jogadorDAO.fecharSessao();
         return false;
     }
 
     private int verificarNumeroCamisaExistente(Short camisa, Short idSelecao)
     {
         JogadorDAO jDAO = new JogadorDAO();
         int resultado;
         resultado = jDAO.queryVerificarCamisaExistente(camisa, idSelecao);
         jDAO.fecharSessao();
         return resultado;
     }
 
     private boolean isNumCamisaVelhaIgualCamisaNova(String numCamisaNovo, String numCamisaAtual)
     {
         if (numCamisaNovo.equals(numCamisaAtual))
         {
             return true;
         }
         return false;
     }
 }
