 package sgcmf.control;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import org.hibernate.HibernateException;
 import org.hibernate.Transaction;
 import sgcmf.hibernate.SGCMFSessionManager;
 import sgcmf.model.dao.CartaoDAO;
 import sgcmf.model.dao.FaltaDAO;
 import sgcmf.model.dao.GolDAO;
 import sgcmf.model.dao.JogadorDAO;
 import sgcmf.model.dao.SubstituicaoDAO;
 import sgcmf.model.hibernate.Jogador;
 import sgcmf.model.hibernate.Selecao;
 import sgcmf.model.hibernate.Usuario;
 import sgcmf.model.other.ResultadoOperacao;
 import sgcmf.model.other.TipoResultadoOperacao;
 
 public class CtrJogador
 {
     public Object[][] queryJogadoresEmCampo(Short idJogo)
     {
         JogadorDAO jDAO;
         Object[][] dadosJogadores;
         ArrayList alJogador;
 
         SGCMFSessionManager.abrirSessao();
         jDAO = JogadorDAO.getInstance();
         alJogador = jDAO.queryJogadoresEmCampo(idJogo);
         dadosJogadores = arrayList2StringMatrix(alJogador);
         SGCMFSessionManager.fecharSessao();
 
         return dadosJogadores;
     }
 
     public Object[][] queryJogadoresEmCampoByNome(Short idJogo, String nome)
     {
         JogadorDAO jDAO;
         Object[][] dadosJogadores;
         ArrayList alJogador;
 
         SGCMFSessionManager.abrirSessao();
         jDAO = JogadorDAO.getInstance();
         alJogador = jDAO.queryJogadoresEmCampoByNome(idJogo, nome);
         dadosJogadores = arrayList2StringMatrix(alJogador);
         SGCMFSessionManager.fecharSessao();
 
         return dadosJogadores;
     }
 
     public Object[][] queryOutrosJogadoresEmCampoSelecao(Short idJogo, Short idSelecao, Short idJogador)
     {
         JogadorDAO jDAO;
         Object[][] dadosJogadores;
         ArrayList alJogador;
 
         SGCMFSessionManager.abrirSessao();
         jDAO = JogadorDAO.getInstance();
 
         alJogador = jDAO.queryOutrosJogadoresEmCampoSelecao(idJogo, idSelecao, idJogador);
         dadosJogadores = arrayList2StringMatrix(alJogador);
 
         SGCMFSessionManager.fecharSessao();
 
         return dadosJogadores;
     }
 
     public Object[][] queryOutrosJogadoresEmCampoSelecaoByNome(Short idJogo, Short idSelecao, Short idJogador, String nome)
     {
         JogadorDAO jDAO;
         Object[][] dadosJogadores;
         ArrayList alJogador;
 
         SGCMFSessionManager.abrirSessao();
         jDAO = JogadorDAO.getInstance();
 
         alJogador = jDAO.queryOutrosJogadoresEmCampoSelecaoByNome(idJogo, idSelecao, idJogador, nome);
         dadosJogadores = arrayList2StringMatrix(alJogador);
         SGCMFSessionManager.fecharSessao();
 
         return dadosJogadores;
     }
 
     public Object[][] queryReservasSelecao(Short idJogo, Short idSelecao)
     {
         JogadorDAO jDAO;
         Object[][] dadosJogadores;
         ArrayList alJogador;
 
         SGCMFSessionManager.abrirSessao();
         jDAO = JogadorDAO.getInstance();
         alJogador = jDAO.queryReservasSelecao(idJogo, idSelecao);
         dadosJogadores = arrayList2StringMatrix(alJogador);
         SGCMFSessionManager.fecharSessao();
 
         return dadosJogadores;
     }
 
     public Object[][] queryReservasSelecaoByNome(Short idJogo, Short idSelecao, String nome)
     {
         JogadorDAO jDAO;
         Object[][] dadosJogadores;
         ArrayList alJogador;
 
         SGCMFSessionManager.abrirSessao();
         jDAO = JogadorDAO.getInstance();
 
         alJogador = jDAO.queryReservasSelecaoByNome(idJogo, idSelecao, nome);
         dadosJogadores = arrayList2StringMatrix(alJogador);
         SGCMFSessionManager.fecharSessao();
 
         return dadosJogadores;
     }
 
     public Short queryIdSelecaoJogador(Short idJogador)
     {
         JogadorDAO jDAO;
         Short idSelecao;
 
         SGCMFSessionManager.abrirSessao();
         jDAO = JogadorDAO.getInstance();
         idSelecao = jDAO.queryIdSelecaoJogador(idJogador);
         SGCMFSessionManager.fecharSessao();
 
         return idSelecao;
     }
 
     public Object[][] queryAllDataJogadorTecnico(Usuario u)
     {
         JogadorDAO jDAO;
         Object[][] dadosJogadores;
         ArrayList alJogador;
        Selecao s;
 
         SGCMFSessionManager.abrirSessao();
         jDAO = JogadorDAO.getInstance();
        Iterator iterator = u.getSelecaos().iterator();

        s = (Selecao) iterator.next();
        alJogador = jDAO.listaTodosBySelecao(s.getId());
         dadosJogadores = arrayList2StringMatrixFull(alJogador);
         SGCMFSessionManager.fecharSessao();
 
         return dadosJogadores;
     }
 
     public Object[][] queryAllDataJogadorByNomeAndByUser(String nome, Usuario u)
     {
         JogadorDAO jDAO;
         Object[][] dadosJogador;
         ArrayList<Jogador> alJogador;
         Iterator iterator = u.getSelecaos().iterator();
         Selecao s;
         s = (Selecao) iterator.next();
         SGCMFSessionManager.abrirSessao();
         jDAO = JogadorDAO.getInstance();
         alJogador = jDAO.queryJogadorByNomeAndByUser(nome, s.getId());
         dadosJogador = arrayList2StringMatrixFull(alJogador);
 
         SGCMFSessionManager.fecharSessao();
 
         return dadosJogador;
     }
 
     public Object[][] queryAllDataJogadorByPosicaoAndByUser(String posicao, Usuario u)
     {
         JogadorDAO jDAO;
         Object[][] dadosJogador;
         ArrayList<Jogador> alJogador;
         Iterator iterator = u.getSelecaos().iterator();
         Selecao s;
         s = (Selecao) iterator.next();
 
         SGCMFSessionManager.abrirSessao();
         jDAO = JogadorDAO.getInstance();
         alJogador = jDAO.queryJogadorByPosicaoAndByUser(posicao, s.getId());
 
         dadosJogador = arrayList2StringMatrixFull(alJogador);
         SGCMFSessionManager.fecharSessao();
 
         return dadosJogador;
     }
 
     private Object[][] arrayList2StringMatrix(ArrayList<Jogador> alJogador)
     {
         Object[][] dadosJogadores;
         Jogador j;
         Selecao s;
 
         dadosJogadores = new Object[alJogador.size()][5];
         for (int i = 0; i < alJogador.size(); i++)
         {
             j = alJogador.get(i);
             dadosJogadores[i][0] = String.valueOf(j.getId());
             s = j.getSelecao();
             dadosJogadores[i][1] = new JLabel(s.getPais(), new ImageIcon(s.getCaminhoimgbandeira()), JLabel.LEFT);
             dadosJogadores[i][2] = String.valueOf(j.getNcamisa());
             dadosJogadores[i][3] = j.getNome();
             dadosJogadores[i][4] = j.getPosicao();
         }
 
         return dadosJogadores;
     }
 
     private Object[][] arrayList2StringMatrixFull(ArrayList<Jogador> alJogador)
     {
         Object[][] dadosJogadores;
         Jogador j;
         String titular;
         Selecao s;
 
         dadosJogadores = new Object[alJogador.size()][8];
         for (int i = 0; i < alJogador.size(); i++)
         {
             j = alJogador.get(i);
             dadosJogadores[i][0] = String.valueOf(j.getId());
             dadosJogadores[i][1] = String.valueOf(j.getNcamisa());
             dadosJogadores[i][2] = j.getNome();
             dadosJogadores[i][3] = String.valueOf(j.getDatanasc());
             dadosJogadores[i][4] = String.valueOf(j.getAltura());
             dadosJogadores[i][5] = j.getPosicao();
             s = j.getSelecao();
             dadosJogadores[i][6] = new JLabel(s.getPais(), new ImageIcon(s.getCaminhoimgbandeira()), JLabel.LEFT);
             if (j.isTitular())
             {
                 titular = "Sim";
             }
             else
             {
                 titular = "Não";
             }
             dadosJogadores[i][7] = new Boolean(j.isTitular());
         }
 
         return dadosJogadores;
     }
 
     public ResultadoOperacao cadastrarJogador(String numCamisa, String nome, String dataNascimento,
             String altura, boolean titular, String posicao, Usuario user)
     {
         JogadorDAO jDAO;
         Short nCamisa;
         Date dtaNascimento;
         BigDecimal aAltura;
         Transaction tr;
         Jogador j = new Jogador();
         ResultadoOperacao result;
         String errorMessege;
         Iterator iterator = user.getSelecaos().iterator();
         Selecao s;
         s = (Selecao) iterator.next();
         boolean bolGoleiro;
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
             SGCMFSessionManager.abrirSessao();
             jDAO = JogadorDAO.getInstance();
             nCamisa = Short.parseShort(numCamisa);
             dtaNascimento = new Date(dataNascimento);
             aAltura = new BigDecimal(altura);
             tr = SGCMFSessionManager.getCurrentSession().beginTransaction();
             try
             {
                 j.setNcamisa(nCamisa);
                 j.setNome(nome);
                 j.setDatanasc(dtaNascimento);
                 j.setAltura(aAltura);
                 j.setTitular(titular);
                 j.setPosicao(posicao);
                 j.setSelecao(s);
                 jDAO.salvar(j);
                 tr.commit();
                 result = new ResultadoOperacao("Jogador Cadastrado com êxito.",
                         TipoResultadoOperacao.EXITO);
             }
             catch (HibernateException he)
             {
                 result = new ResultadoOperacao("Erro no Hibernate.\n" + he.getMessage(),
                         TipoResultadoOperacao.ERRO);
             }
             SGCMFSessionManager.fecharSessao();
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
         JogadorDAO jDAO;
         Short nCamisa;
         Date dataNascimento;
         BigDecimal pAltura;
         Short shortIdJogador;
         Jogador j = new Jogador();
         Transaction tr;
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
             SGCMFSessionManager.abrirSessao();
             jDAO = JogadorDAO.getInstance();
             nCamisa = Short.parseShort(numCamisaNovo);
             dataNascimento = new Date(dtaNascimento);
             pAltura = new BigDecimal(altura);
             tr = SGCMFSessionManager.getCurrentSession().beginTransaction();
             jDAO.carregar(j, shortIdJogador);
             try
             {
                 j.setNcamisa(nCamisa);
                 j.setNome(nome);
                 j.setDatanasc(dataNascimento);
                 j.setAltura(pAltura);
                 j.setPosicao(posicao);
                 jDAO.atualizar(j);
                 tr.commit();
                 result = new ResultadoOperacao("Jogador Alterado com êxito.", TipoResultadoOperacao.EXITO);
             }
             catch (HibernateException he)
             {
                 result = new ResultadoOperacao("Erro no Hibernate.\n" + he.getMessage(), TipoResultadoOperacao.ERRO);
             }
             SGCMFSessionManager.fecharSessao();
         }
         else
         {
             result = new ResultadoOperacao("Falha na alteracao do jogador.\n" + errorMessege, TipoResultadoOperacao.ERRO);
         }
         return result;
     }
 
     public ResultadoOperacao removerJogador(String idJogador)
     {
         int qtdeGols, qtdeFaltas, qtdeCartoes, qtdeSubstituicoes;
         Jogador j = new Jogador();
         Transaction tr;
         JogadorDAO jDAO;
         FaltaDAO faltaDAO;
         CartaoDAO cartaoDAO;
         SubstituicaoDAO substituicaoDAO;
         GolDAO golDAO;
         ResultadoOperacao resultado;
         int qtdeOcorrenciasJogador = 0;
         SGCMFSessionManager.abrirSessao();
         try
         {
             jDAO = JogadorDAO.getInstance();
             golDAO = GolDAO.getInstance();
             faltaDAO = FaltaDAO.getInstance();
             substituicaoDAO = SubstituicaoDAO.getInstance();
             cartaoDAO = CartaoDAO.getInstance();
             tr = SGCMFSessionManager.getCurrentSession().beginTransaction();
             jDAO.carregar(j, new Short(idJogador));
             
             qtdeGols = golDAO.queryQtdeGolsJogador(j.getId());
             qtdeFaltas = faltaDAO.queryQtdeFaltasJogador(j.getId());
             qtdeCartoes = cartaoDAO.queryQtdeCartoesJogador(j.getId());
             qtdeSubstituicoes = substituicaoDAO.queryQtdeSubstituicoesJogador(j.getId());
             qtdeOcorrenciasJogador = qtdeGols + qtdeFaltas + qtdeCartoes + qtdeSubstituicoes;
             
             if (qtdeOcorrenciasJogador == 0)
             {
                 jDAO.apagar(j);
                 tr.commit();
                 resultado = new ResultadoOperacao("Jogador excluido com sucesso.", TipoResultadoOperacao.EXITO);
             }
             else
             {
                 resultado = new ResultadoOperacao("Jogador não pode ser excluído pois há alguma ocorrência referenciada a ele.", TipoResultadoOperacao.ERRO);
             }
         }
         catch (HibernateException he)
         {
             resultado = new ResultadoOperacao("Falha na exclusão do jogador.\n" + he.getMessage(),
                     TipoResultadoOperacao.ERRO);
         }
         SGCMFSessionManager.fecharSessao();
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
         JogadorDAO jogadorDAO;
         int qtdeTitulares;
 
         SGCMFSessionManager.abrirSessao();
         jogadorDAO = JogadorDAO.getInstance();
         qtdeTitulares = jogadorDAO.queryQuantidadeJogadorTitularesSelecao(idSelecao);
         SGCMFSessionManager.fecharSessao();
         return qtdeTitulares;
     }
 
     private int qtdeGoleirosSelecao(Short idSelecao)
     {
         JogadorDAO jogadorDAO;
         int qtdeGoleiros;
 
         SGCMFSessionManager.abrirSessao();
         jogadorDAO = JogadorDAO.getInstance();
         qtdeGoleiros = jogadorDAO.queryQuantidadeGoleirosSelecao(idSelecao);
         SGCMFSessionManager.fecharSessao();
         return qtdeGoleiros;
     }
 
     private boolean isJogadorGoleiro(Short idJogador)
     {
         JogadorDAO jogadorDAO;
         String posicao;
 
         SGCMFSessionManager.abrirSessao();
         jogadorDAO = JogadorDAO.getInstance();
         posicao = jogadorDAO.queryPosicaoJogador(idJogador);
         if (posicao.equals("Goleiro"))
         {
             return true;
         }
         SGCMFSessionManager.fecharSessao();
         return false;
     }
 
     private int verificarNumeroCamisaExistente(Short camisa, Short idSelecao)
     {
         JogadorDAO jDAO;
         int resultado;
 
         SGCMFSessionManager.abrirSessao();
         jDAO = JogadorDAO.getInstance();
         resultado = jDAO.queryVerificarCamisaExistente(camisa, idSelecao);
         SGCMFSessionManager.fecharSessao();
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
