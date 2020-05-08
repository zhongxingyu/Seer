 package com.model.dao;
 
 import com.model.entity.Entity;
 import com.model.entity.Passageiro;
 import com.model.entity.SolicitacaoViagem;
 import com.model.entity.StatusSolicitacaoViagem;
 import com.model.entity.Usuario;
 import com.model.entity.Veiculo;
 import com.model.entity.Viagem;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 /**
  *
  * @author Usuario
  */
 public class ViagemDAO implements Dao {
 
     private Connection connection;
 
     public ViagemDAO(Connection connection) {
         this.connection = connection;
     }
 
     @Override
     public int inserir(Entity entity) {
         Viagem viagem = (Viagem) entity;
         int result = 0;
         String sql = "insert into viagem "
                 + "(autorizante, id_motorista, id_veiculo, "
                 + "data_efetivacao, data_saida, hora_saida, "
                 + "local_saida, data_retorno, hora_retorno, local_retorno, "
                 + "percurso, objetivo) values (?,?,?,?,?,?,?,?,?,?,?,?)";
         try {
             PreparedStatement stmt = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
             stmt.setString(1, viagem.getAutorizante());
             stmt.setInt(2, viagem.getMotorista().getIdUsuario());
             stmt.setInt(3, viagem.getVeiculo().getIdVeiculo());
             stmt.setDate(4, new Date(viagem.getDataEfetivacao().getTime()));
             if (viagem.getDataSaida() != null) {
                 stmt.setDate(5, new Date(viagem.getDataSaida().getTime()));
             } else {
                 stmt.setString(5, null);
             }
             if (viagem.getHoraSaida() != null) {
                 stmt.setTimestamp(6, new Timestamp(viagem.getHoraSaida().getTime()));
             } else {
                 stmt.setString(6, null);
             }
             stmt.setString(7, viagem.getLocalSaida());
             if (viagem.getDataRetorno() != null) {
                 stmt.setDate(8, new Date(viagem.getDataRetorno().getTime()));
             } else {
                 stmt.setString(8, null);
             }
             if (viagem.getHoraRetorno() != null) {
                 stmt.setTimestamp(9, new Timestamp(viagem.getHoraRetorno().getTime()));
             } else {
                 stmt.setString(9, null);
             }
             stmt.setString(10, viagem.getLocalRetorno());
             stmt.setString(11, viagem.getPercurso());
             stmt.setString(12, viagem.getObjetivo());
             result = stmt.executeUpdate();
             ResultSet rsid = stmt.getGeneratedKeys();
             rsid.next();
             Integer gid = rsid.getInt(1);
             viagem.setIdViagem(gid);
             stmt.close();
             SolicitacaoViagemDAO svdao = new SolicitacaoViagemDAO(this.connection);
             for (SolicitacaoViagem sol : viagem.getSolicitacoes(this.connection)) {
                 try {
                     sol.setStatus(StatusSolicitacaoViagem.EFETIVADO.toString());
                     sol.setViagem(viagem);
                     svdao.alterar(sol);
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
             }
             this.cadastrarPassageiros(viagem.getPassageiros(connection), viagem.getIdViagem());
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return result;
     }
 
     private void cadastrarPassageiros(List<Passageiro> passageiros, int idViagem) {
         String sql_insert = "insert into viagem_has_passageiro "
                 + "(id_passageiro, id_viagem) values (?,?)";
         String sql_del = "delete from viagem_has_passageiro "
                 + " where id_viagem=?";
         try {
             PreparedStatement st_del = this.connection.prepareStatement(sql_del);
             st_del.setInt(1, idViagem);
             System.out.println(st_del.toString());
             st_del.executeUpdate();
             st_del.close();
 
             for (Passageiro passageiro : passageiros) {
                 PreparedStatement st_insert = this.connection.prepareStatement(sql_insert);
                 st_insert.setInt(1, passageiro.getIdPassageiro());
                 st_insert.setInt(2, idViagem);
                 System.out.println(st_insert.toString());
                 st_insert.executeUpdate();
                 st_insert.close();
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
 //        for (Passageiro pas : viagem.getPassageiros(this.connection)) {
 //            PreparedStatement stmt2 = this.connection.prepareStatement(sql2);
 //            stmt2.setInt(1, viagem.getIdViagem());
 //            stmt2.setInt(2, pas.getIdPassageiro());
 //            stmt2.execute();
 //        }
     }
 
     @Override
     public int alterar(Entity entity) {
         Viagem viagem = (Viagem) entity;
         int result = 0;
         String sql = "update viagem set "
                + "id_autorizante=?, id_motorista=?, id_veiculo=?, "
                 + "data_efetivacao=? "
                 + "where id_viagem=?";
         try {
             PreparedStatement stmt = this.connection.prepareStatement(sql);
             stmt.setString(1, viagem.getAutorizante());
            stmt.setInt(2, viagem.getMotorista(null).getIdUsuario());
            stmt.setInt(3, viagem.getVeiculo(null).getIdVeiculo());
             stmt.setDate(4, new Date(viagem.getDataEfetivacao().getTime()));
             stmt.setInt(5, viagem.getIdViagem());

         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return result;
     }
 
     @Override
     public int deletar(Entity entity) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public Entity getById(Integer id) {
         try {
             PreparedStatement stmt = this.connection.prepareStatement("select * from viagem where id_viagem=?");
             stmt.setInt(1, id);
             System.out.println(stmt.toString());
             ResultSet rs = stmt.executeQuery();
 
             rs.next();
             Viagem viagem = this.setsFromDatabase(rs);
 
             rs.close();
             stmt.close();
             return viagem;
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public List<Viagem> getViagens() {
         String sql = "select * from viagem";
         List<Viagem> viagens = new ArrayList<Viagem>();
         try {
             PreparedStatement stmt = this.connection.prepareStatement(sql);
             System.out.println(stmt.toString());
             ResultSet rs = stmt.executeQuery();
             while (rs.next()) {
                 Viagem viagem = this.setsFromDatabase(rs);
                 viagens.add(viagem);
             }
             rs.close();
             stmt.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return viagens;
     }
 
     public List<Integer> getPassIds(Integer id) {
         String sql = "select id_passageiro from "
                 + "viagem_has_passageiro where "
                 + "id_viagem=?";
         List<Integer> result = new ArrayList<Integer>();
         try {
             PreparedStatement stmt = this.connection.prepareStatement(sql);
             stmt.setInt(1, id);
             ResultSet res = stmt.executeQuery();
             while (res.next()) {
                 result.add(res.getInt("id_passageiro"));
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return result;
     }
 
     public List<Integer> getSolIds(Integer id) {
         List<Integer> ids = new ArrayList<Integer>();
         String sql = "select id_solicitacao_viagem from "
                 + "solicitacao_viagem where id_viagem=?";
         try {
             PreparedStatement stmt = this.connection.prepareStatement(sql);
             stmt.setInt(1, id);
             ResultSet res = stmt.executeQuery();
             while (res.next()) {
                 ids.add(res.getInt("id_solicitacao_viagem"));
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return ids;
     }
 
     public List<Viagem> getByIdVeiculo(Integer idVeiculo) {
         String sql = "select * from viagem where id_veiculo=?";
         List<Viagem> viagens = new ArrayList<Viagem>();
         try {
             PreparedStatement stmt = this.connection.prepareStatement(sql);
             stmt.setInt(1, idVeiculo);
             System.out.println(stmt.toString());
             ResultSet rs = stmt.executeQuery();
             while (rs.next()) {
                 Viagem viagem = this.setsFromDatabase(rs);
                 viagens.add(viagem);
             }
             rs.close();
             stmt.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return viagens;
     }
     
     public List<Viagem> getByIdMotorista(Integer idUsuario) {
         String sql = "select * from viagem where id_motorista=?";
         List<Viagem> viagens = new ArrayList<Viagem>();
         try {
             PreparedStatement stmt = this.connection.prepareStatement(sql);
             stmt.setInt(1, idUsuario);
             System.out.println(stmt.toString());
             ResultSet rs = stmt.executeQuery();
             while (rs.next()) {
                 Viagem viagem = this.setsFromDatabase(rs);
                 viagens.add(viagem);
             }
             rs.close();
             stmt.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return viagens;
     }
 
     private Viagem setsFromDatabase(ResultSet rs) throws SQLException {
         Viagem viagem = new Viagem();
 
         String autorizante = rs.getString("autorizante");
         viagem.setAutorizante(autorizante);
 
         Calendar c = Calendar.getInstance();
         Date dataEfetivacao = rs.getDate("data_efetivacao");
         if (dataEfetivacao != null) {
             c.setTime(dataEfetivacao);
             viagem.setDataEfetivacao(c.getTime());
         }
 
         viagem.setIdViagem(rs.getInt("id_viagem"));
 
         Usuario motorista = new Usuario();
         motorista.setIdUsuario(rs.getInt("id_motorista"));
         viagem.setMotorista(motorista);
 
         Veiculo veiculo = new Veiculo();
         veiculo.setIdVeiculo(rs.getInt("id_veiculo"));
         viagem.setVeiculo(veiculo);
 
         Date dataSaida = rs.getDate("data_saida");
         if (dataSaida != null) {
             c.setTime(dataSaida);
             viagem.setDataSaida(c.getTime());
         }
         Time horaSaida = rs.getTime("hora_saida");
         System.out.println(horaSaida);
         if (horaSaida != null) {
             viagem.setHoraSaida(horaSaida);
         }
         c = Calendar.getInstance();
         Date dataRetorno = rs.getDate("data_retorno");
         if (dataRetorno != null) {
             c.setTime(dataRetorno);
             viagem.setDataRetorno(c.getTime());
         }
         Time horaRetorno = rs.getTime("hora_retorno");
         if (horaRetorno != null) {
             viagem.setHoraRetorno(horaRetorno);
         }
 
         viagem.setLocalRetorno(rs.getString("local_retorno"));
         viagem.setLocalSaida(rs.getString("local_saida"));
         viagem.setObjetivo(rs.getString("objetivo"));
         viagem.setPercurso(rs.getString("percurso"));
 
         return viagem;
     }
 }
