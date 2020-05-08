 package ono.leo.erp.efd.pis_cofins;
 
 import java.io.PrintWriter;
 import ono.leo.erp.efd.pis_cofins.bloco0.Registro0000;
 import ono.leo.erp.efd.pis_cofins.bloco9.Registro9999;
 
 /**
  * Registro root e o registro inicial em que sao armazedos os registros 
  * iniciais de nivel 0, ou seja, os registros 0000 e 9999.
  * 
  * Para o programador, e o ponto de partida e atraves deste objeto
  * e possivel adicionar os demais registros e gerar o arquivo EFD-PIS/Cofins.
  * 
  * Exemplo:
  * 
  * RegistroRoot rr = new RegistroRoot();
  * Registro0100 r0100 = new Registro0100();
  * rr.getRegistro0000().getRegistro0001().addRegistroFilho(r0100);
  * rr.gerar("c:/arquivo.txt");
  *
  * @author Leonardo Ono (ono.leo@gmail.com)
  * @since 1.00.00 (10/05/2011 16:41)
  */
 public class RegistroRoot extends Registro {
 
     private Registro0000 registro0000 = new Registro0000();
     private Registro9999 registro9999 = new Registro9999();
     
     public RegistroRoot() {
         REG = "ROOT";
         REG_PAI = null;
         nivel = -1;
         obrigatoriedade = Obrigatoriedade.O;
         super.addRegistroFilho(registro0000);
         super.addRegistroFilho(registro9999);
     }
 
     public Registro0000 getRegistro0000() {
         return registro0000;
     }
 
     public Registro9999 getRegistro9999() {
         return registro9999;
     }
     
     public void gerar(String caminho) throws Exception {
         registro0000.atualizarQuantidadeNosRegistrosDeEncerramentoDeBloco();
         // Atualiza quantidade total de registros
         registro9999.setQTD_LIN(getQuantidadeTotalDeRegistros() + "");
         
         PrintWriter pw = null;
         try {
             pw = new PrintWriter(caminho);
            pw.print(this.gerarLinha());
             pw.close();
         } catch (Exception ex) {
             throw ex;
         } finally {
             pw.close();
         }
     }
 
     @Override
     public void addRegistroFilho(Registro registro) {
         throw new RuntimeException(
                   "Nao e possivel adicionar registros filhos "
                 + "no registro root ! \nAo inves disso, utilize conforme "
                 + "exemplo abaixo: \n\n"
                 + "RegistroRoot rr = new RegistroRoot(); \n"
                 + "Registro0100 r0100 = new Registro0100(); \n"
                 + "rr.getRegistro0000().getRegistro0001()"
                 + ".addRegistroFilho(r0100); \n");
     }
     
     @Override
     public int getQuantidadeTotalDeRegistros() {
         // Retorna menos um pois este registro root nao conta
         return super.getQuantidadeTotalDeRegistros() - 1;
     }
 
 }
