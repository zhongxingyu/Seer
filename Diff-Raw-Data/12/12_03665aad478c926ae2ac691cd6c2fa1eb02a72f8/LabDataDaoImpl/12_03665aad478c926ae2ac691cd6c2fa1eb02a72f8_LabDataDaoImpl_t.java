 package com.solidstategroup.radar.dao.impl;
 
 import com.solidstategroup.radar.dao.LabDataDao;
 import com.solidstategroup.radar.model.sequenced.LabData;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
 
 import javax.sql.DataSource;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class LabDataDaoImpl extends BaseDaoImpl implements LabDataDao {
     private SimpleJdbcInsert labDataInsert;
     private static final Logger LOGGER = LoggerFactory.getLogger(LabDataDaoImpl.class);
 
     @Override
     public void setDataSource(DataSource dataSource) {
         // Call super
         super.setDataSource(dataSource);
 
         // Initialise a simple JDBC insert to be able to get the allocated ID
         labDataInsert = new SimpleJdbcInsert(dataSource).withTableName("tbl_LabData")
                 .usingGeneratedKeyColumns("labID").usingColumns("RADAR_NO", "DATE_LAB_RES", "CREAT_SER", "PROTEIN",
                         "ALBUMIN", "UREA_SER", "SODIUM", "POTASSIUM", "PHOS", "PROT_CREAT_RAT", "ALB_CREAT_RAT",
                         "WBC", "HB", "NEUTRO", "PLATELETS", "FERRITIN", "CHOL_TOTAL", "CHOL_HDL", "CHOL_LDL", "TRIG",
                         "CREAT_CLEAR_24_URINE", "CREAT_CLEAR_RADIO", "CREAT_CLEAR_SCHZ", "THYROX", "TSH", "ANCA",
                         "ELISA_ASS", "ENA", "ANA", "DNA_ANTIB", "DNA_ANTI_DS", "CRYOGLOB", "ANTI_GBM", "IGG", "IGA",
                         "IGM", "COMP_C3", "COMP_C4", "COMP_OTHER", "C3_NEPH_FAC", "ANTI_SLT", "INR", "CRP",
                         "ANTI_STREP_O", "HEP_B", "HEP_C", "HIV", "DNA_FACTOR_H", "EBV", "CMV", "CMV_SYM", "BKV",
                         "BKV_SYM", "HANTAVIRUS", "PARVO_ANTIB", "OTHER_INFECT", "OTHER_INFECT_SP",
                         "UR_VOL_24H", "UR_VOL_24H_COND", "HAEMATURIA", "ALBUMINURIA", "DYS_ERYTH_URINE",
                         "RED_CCASTS_URINE", "WBC_CASTS_URINE", "LEUC_URINE", "NITRITE", "BACT_URINE", "GLUC_URINE",
                         "OSMOLARITY", "PROTEINURIA_DIP", "SEQ_NO", "ANTI_CLQ");
     }
 
     public void saveLabData(final LabData labData) {
         Map<String, Object> labDataMap = new HashMap<String, Object>() {
             {
                 put("RADAR_NO", labData.getRadarNumber());
                 put("DATE_LAB_RES", labData.getDate());
                 put("CREAT_SER", labData.getSerumCreatinine());
                 put("PROTEIN", labData.getProtein());
                 put("ALBUMIN", labData.getAlbumin());
                 put("UREA_SER", labData.getBun());
                 put("SODIUM", labData.getSodium());
                 put("POTASSIUM", labData.getPotassium());
                 put("PHOS", labData.getPhosphate());
                 put("PROT_CREAT_RAT", labData.getProteinCreatinineRatio());
                 put("ALB_CREAT_RAT", labData.getAlbuminCreatinineRatio());
                 put("WBC", labData.getWbc());
                 put("HB", labData.getHb());
                 put("NEUTRO", labData.getNeutrophils());
                 put("PLATELETS", labData.getPlatelets());
                 put("FERRITIN", labData.getFerritin());
                 put("CHOL_TOTAL", labData.getTotalCholesterol());
                 put("CHOL_HDL", labData.getHdlCholesterol());
                 put("CHOL_LDL", labData.getLdlCholesterol());
                 put("TRIG", labData.getTriglycerides());
                 put("CREAT_CLEAR_24_URINE", null);
                 put("CREAT_CLEAR_RADIO", null);
                 put("CREAT_CLEAR_SCHZ", labData.getCreatinineClearance());
                 put("THYROX", labData.getThyroxine());
                 put("TSH", labData.getTsh());
                 put("ANCA", labData.getAnca() != null ? labData.getAnca().getId() : null);
                 put("ELISA_ASS", null);
                 put("ENA", labData.getEna() != null ? labData.getEna().getId() : null);
                 put("ANA", labData.getAna() != null ? labData.getAna().getId() : null);
                 put("DNA_ANTIB", labData.getDnaAntibodies());
                 put("DNA_ANTI_DS", labData.getAntiDsDna() != null ? labData.getAntiDsDna().getId() : null);
                 put("CRYOGLOB", labData.getCryoglobulins() != null ? labData.getCryoglobulins().getId() : null);
                 put("ANTI_GBM", labData.getAntiGbm() != null ? labData.getAntiGbm().getId() : null);
                 put("IGG", labData.getIgG());
                 put("IGA", labData.getIgA());
                 put("IGM", labData.getIgM());
                 put("COMP_C3", labData.getComplementC3());
                 put("COMP_C4", labData.getComplementC4());
                 put("COMP_OTHER", labData.getComplementOther());
                 put("C3_NEPH_FAC", labData.getC3NephriticFactor() != null ? labData.getC3NephriticFactor().getId() :
                         null);
                 put("ANTI_SLT", null);
                 put("INR", labData.getInr());
                 put("CRP", labData.getCrp());
                 put("ANTI_STREP_O", labData.getAntistreptolysin());
                 put("HEP_B", labData.getHepatitisB() != null ? labData.getHepatitisB().getId() : null);
                 put("HEP_C", labData.getHepatitisC() != null ? labData.getHepatitisC().getId() : null);
                 put("HIV", labData.getHivAntibody() != null ? labData.getHivAntibody().getId() : null);
                 put("DNA_FACTOR_H", labData.getDnaTakenFactorH());
                 put("EBV", labData.getEbv() != null ? labData.getEbv().getId() : null);
                 put("CMV", labData.getCmvSerology() != null ? labData.getCmvSerology().getId() : null);
                 put("CMV_SYM", labData.getCmvSymptomatic());
                 put("BKV", null);
                 put("BKV_SYM", null);
                 put("HANTAVIRUS", null);
                 put("PARVO_ANTIB", labData.getParvovirusAntibody() != null ? labData.getParvovirusAntibody().getId() :
                         null);
                 put("OTHER_INFECT", labData.getOtherInfection());
                 put("OTHER_INFECT_SP", labData.getOtherInfectionDetail());
                 put("UR_VOL_24H", labData.getUrineVolume());
                 put("UR_VOL_24H_COND", labData.getUrineVolumeCondition() != null ?
                         labData.getUrineVolumeCondition().getId() : null);
                 put("HAEMATURIA", labData.getHaematuria() != null ? labData.getHaematuria().getId() : null);
                 put("ALBUMINURIA", labData.getAlbuminuria() != null ? labData.getAlbuminuria().getId() : null);
                 put("DYS_ERYTH_URINE", labData.getDysmorphicErythrocytes() != null ?
                         labData.getDysmorphicErythrocytes().getId() : null);
                 put("RED_CCASTS_URINE", labData.getRedCellCast() != null ? labData.getRedCellCast().getId() : null);
                 put("WBC_CASTS_URINE", labData.getWhiteCellCasts() != null ? labData.getWhiteCellCasts().getId() : null);
                 put("LEUC_URINE", labData.getLeucocytesUrine());
                 put("NITRITE", labData.getNitrite());
                 put("BACT_URINE", labData.getBacteria());
                 put("GLUC_URINE", labData.getGlucose());
                 put("OSMOLARITY", labData.getOsmolality());
                 put("PROTEINURIA_DIP", labData.getProteinuria() != null ? labData.getProteinuria().getId() : null);
                 put("SEQ_NO", labData.getSequenceNumber());
                 put("ANTI_CLQ", labData.getAntiClqAntibodies());
             }
         };
 
         if (labData.hasValidId()) {
             labDataMap.put("labID", labData.getId());
             namedParameterJdbcTemplate.update("UPDATE tbl_LabData " +
                     "SET " +
                     "RADAR_NO = :RADAR_NO, " +
                     "DATE_LAB_RES = :DATE_LAB_RES, " +
                     "CREAT_SER = :CREAT_SER, " +
                     "PROTEIN = :PROTEIN, " +
                     "ALBUMIN = :ALBUMIN, " +
                     "UREA_SER = :UREA_SER, " +
                     "SODIUM = :SODIUM, " +
                     "POTASSIUM = :POTASSIUM, " +
                     "PHOS = :PHOS, " +
                     "PROT_CREAT_RAT = :PROT_CREAT_RAT, " +
                     "ALB_CREAT_RAT = :ALB_CREAT_RAT, " +
                     "WBC = :WBC, " +
                     "HB = :HB, " +
                     "NEUTRO = :NEUTRO, " +
                     "PLATELETS = :PLATELETS, " +
                     "FERRITIN = :FERRITIN, " +
                     "CHOL_TOTAL = :CHOL_TOTAL, " +
                     "CHOL_HDL = :CHOL_HDL, " +
                     "CHOL_LDL = :CHOL_LDL, " +
                     "TRIG = :TRIG, " +
                     "CREAT_CLEAR_24_URINE = :CREAT_CLEAR_24_URINE, " +
                     "CREAT_CLEAR_RADIO = :CREAT_CLEAR_RADIO, " +
                     "CREAT_CLEAR_SCHZ = :CREAT_CLEAR_SCHZ, " +
                     "THYROX = :THYROX, " +
                     "TSH = :TSH, " +
                     "ANCA = :ANCA," +
                     "ELISA_ASS = :ELISA_ASS, " +
                     "ENA = :ENA, " +
                     "ANA = :ANA, " +
                     "DNA_ANTIB = :DNA_ANTIB, " +
                     "DNA_ANTI_DS = :DNA_ANTI_DS, " +
                     "CRYOGLOB = :CRYOGLOB, " +
                     "ANTI_GBM = :ANTI_GBM, " +
                     "IGG = :IGG, " +
                     "IGA = :IGA, " +
                     "IGM = :IGM, " +
                     "COMP_C3 = :COMP_C3, " +
                     "COMP_C4 = :COMP_C4, " +
                     "COMP_OTHER = :COMP_OTHER, " +
                     "C3_NEPH_FAC = :C3_NEPH_FAC, " +
                     "ANTI_SLT = :ANTI_SLT, " +
                     "INR = :INR, " +
                     "CRP = :CRP, " +
                     "ANTI_STREP_O = :ANTI_STREP_O, " +
                     "HEP_B = :HEP_B, " +
                     "HEP_C = :HEP_C, " +
                     "HIV = :HIV, " +
                     "DNA_FACTOR_H = :DNA_FACTOR_H, " +
                     "EBV = :EBV, " +
                     "CMV = :CMV, " +
                     "CMV_SYM = :CMV_SYM, " +
                     "BKV = :BKV, " +
                     "BKV_SYM = :BKV_SYM, " +
                     "HANTAVIRUS = :HANTAVIRUS, " +
                     "PARVO_ANTIB = :PARVO_ANTIB, " +
                     "OTHER_INFECT = :OTHER_INFECT, " +
                     "OTHER_INFECT_SP = :OTHER_INFECT_SP, " +
                     "UR_VOL_24H = :UR_VOL_24H, " +
                     "UR_VOL_24H_COND = :UR_VOL_24H_COND, " +
                     "HAEMATURIA = :HAEMATURIA, " +
                     "ALBUMINURIA = :ALBUMINURIA, " +
                     "DYS_ERYTH_URINE = :DYS_ERYTH_URINE, " +
                     "RED_CCASTS_URINE = :RED_CCASTS_URINE, " +
                     "WBC_CASTS_URINE = :WBC_CASTS_URINE, " +
                     "LEUC_URINE = :LEUC_URINE, " +
                     "NITRITE = :NITRITE, " +
                     "BACT_URINE = :BACT_URINE, " +
                     "GLUC_URINE = :GLUC_URINE, " +
                     "OSMOLARITY = :OSMOLARITY, " +
                     "PROTEINURIA_DIP = :PROTEINURIA_DIP, " +
                     "SEQ_NO = :SEQ_NO, " +
                     "ANTI_CLQ = :ANTI_CLQ " +
                     " WHERE labID = :labID; ", labDataMap);
         } else {
             Number id = labDataInsert.executeAndReturnKey(labDataMap);
             labData.setId(id.longValue());
         }
     }
 
     public LabData getLabData(long id) {
         try {
             return jdbcTemplate.queryForObject("SELECT * FROM tbl_LabData WHERE labID = ?",
                     new Object[]{id}, new LabDataRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not get record for lab data with ID {}", id);
             return null;
         }
     }
 
     public List<LabData> getLabDataByRadarNumber(long id) {
         return jdbcTemplate.query("SELECT * FROM tbl_LabData WHERE RADAR_NO = ?",
                 new Object[]{id}, new LabDataRowMapper());
     }
 
     public LabData getFirstLabDataByRadarNumber(Long id) {
         try {
             return jdbcTemplate.queryForObject("SELECT * FROM tbl_LabData WHERE RADAR_NO = ? AND SEQ_NO = 1",
                     new Object[]{id}, new LabDataRowMapper());
         } catch (EmptyResultDataAccessException e) {
             return null;
        } catch (IncorrectResultSizeDataAccessException e) {
            LOGGER.error("Multiple lab data records exists with sequence number 1 for radar number {}, " +
                    "only one record should exist for sequence number 1", id);
            List<LabData> labDatas = jdbcTemplate.query("SELECT * FROM tbl_LabData WHERE RADAR_NO = ? AND SEQ_NO = 1",
                    new Object[]{id}, new LabDataRowMapper());
            if(!labDatas.isEmpty()) {
                return labDatas.get(0);
            }
         }
        return null;
     }
 
     private class LabDataRowMapper implements RowMapper<LabData> {
         public LabData mapRow(ResultSet resultSet, int i) throws SQLException {
             // Construct a lab data object and set all the fields
             LabData labData = new LabData();
 
             labData.setId(resultSet.getLong("labID"));
             labData.setRadarNumber(resultSet.getLong("RADAR_NO"));
             labData.setDate(resultSet.getDate("DATE_LAB_RES"));
 
             // Blood bits
             labData.setSerumCreatinine(getDoubleWithNullCheck("CREAT_SER", resultSet));
             labData.setProtein(getDoubleWithNullCheck("PROTEIN", resultSet));
             labData.setAlbumin(getDoubleWithNullCheck("ALBUMIN", resultSet));
             labData.setBun(getDoubleWithNullCheck("UREA_SER", resultSet));
             labData.setSodium(getDoubleWithNullCheck("SODIUM", resultSet));
             labData.setPotassium(getDoubleWithNullCheck("POTASSIUM", resultSet));
             labData.setPhosphate(getDoubleWithNullCheck("PHOS", resultSet));
             labData.setProteinCreatinineRatio(getDoubleWithNullCheck("PROT_CREAT_RAT", resultSet));
             labData.setAlbuminCreatinineRatio(getDoubleWithNullCheck("ALB_CREAT_RAT", resultSet));
             labData.setWbc(getDoubleWithNullCheck("WBC", resultSet));
             labData.setHb(getDoubleWithNullCheck("HB", resultSet));
             labData.setNeutrophils(getDoubleWithNullCheck("NEUTRO", resultSet));
             labData.setPlatelets(getDoubleWithNullCheck("PLATELETS", resultSet));
             labData.setFerritin(getDoubleWithNullCheck("FERRITIN", resultSet));
 
             // Cholesterol
             labData.setTotalCholesterol(getDoubleWithNullCheck("CHOL_TOTAL", resultSet));
             labData.setHdlCholesterol(getDoubleWithNullCheck("CHOL_HDL", resultSet));
             labData.setLdlCholesterol(getDoubleWithNullCheck("CHOL_HDL", resultSet));
 
             labData.setTriglycerides(getDoubleWithNullCheck("TRIG", resultSet));
 
             // There is a row CREAT_CLEAR_24_URINE but all references are commented out in legacy ASP code
             // Same goes for CREAT_CLEAR_RADIO
 
             labData.setCreatinineClearance(getDoubleWithNullCheck("CREAT_CLEAR_SCHZ", resultSet));
 
             labData.setThyroxine(getDoubleWithNullCheck("THYROX", resultSet));
             labData.setTsh(getDoubleWithNullCheck("TSH", resultSet));
 
             labData.setAnca(BaseDaoImpl.<LabData.Anca>getEnumValue(LabData.Anca.class, getIntegerWithNullCheck("ANCA", resultSet)));
 
             // There is a row ELISA_ASS but again it is all commented out in current code
 
             labData.setEna(BaseDaoImpl
                     .<LabData.PositiveNegativeNotDone>getEnumValue(LabData.PositiveNegativeNotDone.class,
                             getIntegerWithNullCheck("ENA", resultSet)));
             labData.setAna(BaseDaoImpl
                     .<LabData.PositiveNegativeNotDone>getEnumValue(LabData.PositiveNegativeNotDone.class,
                             getIntegerWithNullCheck("ANA", resultSet)));
 
             labData.setDnaAntibodies(resultSet.getString("DNA_ANTIB"));
             labData.setAntiDsDna(BaseDaoImpl
                     .<LabData.PositiveNegativeNotDone>getEnumValue(LabData.PositiveNegativeNotDone.class,
                             getIntegerWithNullCheck("DNA_ANTI_DS", resultSet)));
 
             labData.setCryoglobulins(BaseDaoImpl
                     .<LabData.PositiveNegativeNotDone>getEnumValue(LabData.PositiveNegativeNotDone.class,
                             getIntegerWithNullCheck("CRYOGLOB", resultSet)));
             labData.setAntiGbm(BaseDaoImpl
                     .<LabData.PositiveNegativeNotDone>getEnumValue(LabData.PositiveNegativeNotDone.class,
                             getIntegerWithNullCheck("ANTI_GBM", resultSet)));
 
             labData.setIgG(getDoubleWithNullCheck("IGG", resultSet));
             labData.setIgA(getDoubleWithNullCheck("IGA", resultSet));
             labData.setIgM(getDoubleWithNullCheck("IGM", resultSet));
 
             labData.setComplementC3(getDoubleWithNullCheck("COMP_C3", resultSet));
             labData.setComplementC4(getDoubleWithNullCheck("COMP_C4", resultSet));
             labData.setComplementOther(resultSet.getString("COMP_OTHER"));
             labData.setComplementOtherSelected(resultSet.getString("COMP_OTHER") != null);
 
             labData.setC3NephriticFactor(BaseDaoImpl
                     .<LabData.PositiveNegativeUnknown>getEnumValue(LabData.PositiveNegativeUnknown.class,
                             getIntegerWithNullCheck("C3_NEPH_FAC", resultSet)));
 
             // There is an ANTI_SLT column that doesn't seem to be used within legacy code
 
             labData.setInr(getDoubleWithNullCheck("INR", resultSet));
             labData.setCrp(getDoubleWithNullCheck("CRP", resultSet));
 
             labData.setAntistreptolysin(getDoubleWithNullCheck("ANTI_STREP_O", resultSet));
 
             labData.setHepatitisB(BaseDaoImpl.<LabData.PositiveNegativeUnknown>getEnumValue(
                     LabData.PositiveNegativeUnknown.class, getIntegerWithNullCheck("HEP_B", resultSet)));
             labData.setHepatitisC(BaseDaoImpl.<LabData.PositiveNegativeUnknown>getEnumValue(
                     LabData.PositiveNegativeUnknown.class, getIntegerWithNullCheck("HEP_C", resultSet)));
 
             labData.setHivAntibody(BaseDaoImpl.<LabData.PositiveNegativeUnknown>getEnumValue(
                     LabData.PositiveNegativeUnknown.class, getIntegerWithNullCheck("HIV", resultSet)));
 
             labData.setDnaTakenFactorH(resultSet.getBoolean("DNA_FACTOR_H"));
 
             labData.setEbv(BaseDaoImpl
                     .<LabData.Immunoglobulins>getEnumValue(LabData.Immunoglobulins.class, getIntegerWithNullCheck("EBV", resultSet)));
             labData.setCmvSerology(BaseDaoImpl
                     .<LabData.Immunoglobulins>getEnumValue(LabData.Immunoglobulins.class, getIntegerWithNullCheck("CMV", resultSet)));
             labData.setCmvSymptomatic(resultSet.getBoolean("CMV_SYM"));
 
             // Three more commented within code: BKV and BKV_SYM and HANTAVIRUS
 
             labData.setParvovirusAntibody(BaseDaoImpl.<LabData.PositiveNegativeNotDone>getEnumValue(
                     LabData.PositiveNegativeNotDone.class, getIntegerWithNullCheck("PARVO_ANTIB", resultSet)));
             labData.setOtherInfection(resultSet.getBoolean("OTHER_INFECT"));
             labData.setOtherInfectionDetail(resultSet.getString("OTHER_INFECT_SP"));
 
             labData.setUrineVolume(getDoubleWithNullCheck("UR_VOL_24H", resultSet));
             labData.setUrineVolumeCondition(BaseDaoImpl.<LabData.UrineVolumeCondition>getEnumValue(
                     LabData.UrineVolumeCondition.class, getIntegerWithNullCheck("UR_VOL_24H_COND", resultSet)));
 
             labData.setHaematuria(BaseDaoImpl
                     .<LabData.Haematuria>getEnumValue(LabData.Haematuria.class, getIntegerWithNullCheck("HAEMATURIA", resultSet)));
             labData.setAlbuminuria(BaseDaoImpl
                     .<LabData.Albuminuria>getEnumValue(LabData.Albuminuria.class, getIntegerWithNullCheck("ALBUMINURIA"
                             , resultSet)));
 
             labData.setDysmorphicErythrocytes(BaseDaoImpl
                     .<LabData.Present>getEnumValue(LabData.Present.class, getIntegerWithNullCheck("DYS_ERYTH_URINE", resultSet)));
             labData.setRedCellCast(BaseDaoImpl
                     .<LabData.Present>getEnumValue(LabData.Present.class, getIntegerWithNullCheck("RED_CCASTS_URINE", resultSet)));
             labData.setWhiteCellCasts(BaseDaoImpl
                     .<LabData.Present>getEnumValue(LabData.Present.class, getIntegerWithNullCheck("WBC_CASTS_URINE", resultSet)));
 
             labData.setLeucocytesUrine(getBooleanWithNullCheck("LEUC_URINE", resultSet));
             labData.setNitrite(getBooleanWithNullCheck("NITRITE", resultSet));
             labData.setBacteria(resultSet.getBoolean("BACT_URINE"));
             labData.setGlucose(getBooleanWithNullCheck("GLUC_URINE", resultSet));
 
             // Osmolarity is within the database as varchar but validated on legacy frontend as integer
             // So within our model keep it as a double and do the conversion here
             labData.setOsmolality(getDoubleWithNullCheck("OSMOLARITY", resultSet));
 
             labData.setProteinuria(BaseDaoImpl.<LabData.Proteinuria>getEnumValue(LabData.Proteinuria.class,
                     getIntegerWithNullCheck("PROTEINURIA_DIP", resultSet)));
 
             labData.setSequenceNumber(getIntegerWithNullCheck("SEQ_NO", resultSet));
             labData.setAntiClqAntibodies(getDoubleWithNullCheck("ANTI_CLQ", resultSet));
 
             return labData;
         }
     }
 
 }
