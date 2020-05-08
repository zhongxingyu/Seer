 package sistemasExternos;
 
import java.util.HashMap;
import java.util.Date;
 import java.security.MessageDigest;
 import java.util.GregorianCalendar;
 
 public class BancoMackenzie extends Banco {
     
     public BancoMackenzie()
     {
         Cliente cliente = new Cliente();
         cliente.nome = "Rafael";
         cliente.contas.add("001");
         cliente.contas.add("002");
 
         Conta conta = new Conta();
         conta.numeroConta = "001";
         conta.senha = "1234";
         conta.saldo = 10000;
         conta.cliente = cliente;
 
         m_contas.put(conta.numeroConta, conta);
 
         Conta conta2 = new Conta();
         conta2.numeroConta = "002";
         conta2.senha = "4321";
         conta2.saldo = 400;
         conta2.cliente = cliente;
 
         m_contas.put(conta2.numeroConta, conta2);
     }
     
     public String iniciarSessao(String conta, String senha)
     {
         if(!m_contas.containsKey(conta))
         {
             setLastError("Conta invalida");
             return "";
         }
         
         if(!m_contas.get(conta).senha.equals(senha))
         {
             setLastError("Senha invalida");
             return "";
         }
         
         Sessao sessao = new Sessao();      
         sessao.contaSessao = conta;
         sessao.ultimaOperacao = new Date();
         
         try
         {
             String chave = sessao.ultimaOperacao.toString();
             MessageDigest md = MessageDigest.getInstance("MD5");
             md.reset();
             md.update(chave.getBytes());
             byte[] hashMd5 = md.digest();
             
             StringBuilder s = new StringBuilder();      
             for (int i = 0; i < hashMd5.length; i++) {
                 int parteAlta = ((hashMd5[i] >> 4) & 0xf) << 4;
                 int parteBaixa = hashMd5[i] & 0xf;
                 if (parteAlta == 0) s.append('0');
                 s.append(Integer.toHexString(parteAlta | parteBaixa));
             }
             
             sessao.chaveSessao = s.toString();
         }
         catch (Exception e){
         }
 
         m_sessoes.put(sessao.chaveSessao, sessao);
 
         return sessao.chaveSessao;
     }
     
     boolean validaSessao(String sessao)
     {
         if(!m_sessoes.containsKey(sessao))
         {
             setLastError("Sessao invalida");
             return false;
         }
         
         GregorianCalendar gc=new GregorianCalendar();
         gc.setTime(m_sessoes.get(sessao).ultimaOperacao);
        gc.add(gc.SECOND, 120);
         
         if(gc.getTime().before(new Date()))
         {
             m_sessoes.remove(sessao);
             setLastError("Sessao expirada");
             return false;
         }
         
         return true;
     }
     
     public boolean aprovarSaque(String sessao, double valor)
     {
         if(!validaSessao(sessao))
         {
             return false;
         }
 
         Conta conta = m_contas.get(m_sessoes.get(sessao).contaSessao);
 
         if(conta.saldo < valor)
         {
             setLastError("Saldo insuficiente");
             return false;
         }
 
         conta.saldo -= valor;
 
         return true;
     }
     
     private HashMap<String, Conta> m_contas = new HashMap<String, Conta>();
     private HashMap<String, Sessao> m_sessoes = new HashMap<String, Sessao>();
 }
