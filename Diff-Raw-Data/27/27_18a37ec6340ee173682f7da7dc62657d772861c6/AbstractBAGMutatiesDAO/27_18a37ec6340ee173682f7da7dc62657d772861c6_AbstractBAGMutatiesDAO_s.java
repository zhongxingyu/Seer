 /**
  * Copyright 2013 Ordina
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package nl.ordina.bag.etl.dao;
 
import java.io.IOException;
import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.List;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.namespace.QName;
 
 import nl.kadaster.schemas.bag_verstrekkingen.extract_producten_lvc.v20090901.MutatieProduct;
 import nl.ordina.bag.etl.Constants.BAGObjectType;
 import nl.ordina.bag.etl.Constants.ProcessingStatus;
 import nl.ordina.bag.etl.model.mutatie.BAGMutatie;
 import nl.ordina.bag.etl.model.mutatie.MutatiesFile;
 import nl.ordina.bag.etl.xml.XMLMessageBuilder;
 
import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.IncorrectResultSizeDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.PreparedStatementCreator;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.support.GeneratedKeyHolder;
 import org.springframework.jdbc.support.KeyHolder;
 import org.springframework.transaction.support.TransactionCallback;
 import org.springframework.transaction.support.TransactionTemplate;
 
 public abstract class AbstractBAGMutatiesDAO implements BAGMutatiesDAO
 {
   protected transient Log logger = LogFactory.getLog(getClass());
 	protected TransactionTemplate transactionTemplate;
 	protected JdbcTemplate jdbcTemplate;
 
 	public AbstractBAGMutatiesDAO(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate)
 	{
 		this.transactionTemplate = transactionTemplate;
 		this.jdbcTemplate = jdbcTemplate;
 	}
 
 	@Override
 	public void doInTransaction(final TransactionCallback<?> callback) throws DataAccessException
 	{
 		transactionTemplate.execute(callback);
 	}
 
 	@Override
 	public boolean existsMutatiesFile(java.util.Date dateFrom) throws DAOException
 	{
 		try
 		{
 			return jdbcTemplate.queryForInt(
 				"select count(id)" +
 				" from bag_mutaties_file" +
 				" where date_from = trunc(?)",
 			dateFrom) > 0;
 		}
 		catch (DataAccessException e)
 		{
 			throw new DAOException(e);
 		}
 	}
 
 	@Override
 	public long insertMutatiesFile(final java.util.Date dateFrom, final java.util.Date dateTo, final byte[] content) throws DAOException
 	{
 		try
 		{
 			KeyHolder keyHolder = new GeneratedKeyHolder();
 			jdbcTemplate.update(
 				new PreparedStatementCreator()
 				{
 					@Override
 					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException
 					{
 						PreparedStatement ps = connection.prepareStatement(
 							"insert into bag_mutaties_file (" +
 							" id," +
 							" date_from," +
 							" date_to," +
 							" content" +
 							") values ((select nvl(max(id),0) + 1 from bag_mutaties_file),trunc(?),trunc(?),?)",
 							new int[]{1}
 						);
 						ps.setDate(1,new Date(dateFrom.getTime()));
 						ps.setDate(2,new Date(dateTo.getTime()));
 						ps.setBytes(3,content);
 						return ps;
 					}
 				},
 				keyHolder
 			);
 			return keyHolder.getKey().longValue();
 		}
 		catch (DataAccessException e)
 		{
 			throw new DAOException(e);
 		}
 	}
 
 	@Override
 	public MutatiesFile getNexMutatiesFile() throws DAOException
 	{
 		try
 		{
 			MutatiesFile file = jdbcTemplate.queryForObject(
 				"select *" +
 				" from bag_mutaties_file" +
 				" where date_from = (select (max(date_to)) from bag_mutaties_file where status = " + ProcessingStatus.PROCESSED.ordinal() + ")",// + 
 				//" union all" + 
 				//" select *" +
 				//" from bag_mutaties_file" +
 				//" where date_from = (select (min(date_from)) from bag_mutaties_file where status = " + ProcessingStatus.UNPROCESSED.ordinal() + 
 				//" and (select count(id) from bag_mutaties_file where status = " + ProcessingStatus.PROCESSED.ordinal() + ") = 0)",
 				new RowMapper<MutatiesFile>()
 				{
 					@Override
 					public MutatiesFile mapRow(ResultSet rs, int row) throws SQLException
 					{
						try
						{
							MutatiesFile object = new MutatiesFile();
							object.setId(rs.getLong("id"));
							object.setDateFrom(rs.getDate("date_from"));
							object.setDateTo(rs.getDate("date_to"));
							Blob blob = rs.getBlob("content");
							object.setStatus(ProcessingStatus.values()[rs.getInt("status")]);
							object.setContent(IOUtils.toByteArray(blob.getBinaryStream()));
							return object;
						}
						catch (IOException e)
						{
							throw new SQLException(e);
						}
 					}
 				}
 			);
 			return file;
 		}
 		catch (IncorrectResultSizeDataAccessException e)
 		{
 			return null;
 		}
 		catch (DataAccessException e)
 		{
 			throw new DAOException(e);
 		}
 	}
 
 	@Override
 	public void setMutatiesFileStatus(final long id, final ProcessingStatus status) throws DAOException
 	{
 		try
 		{
 			jdbcTemplate.update(
 				new PreparedStatementCreator()
 				{
 					@Override
 					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException
 					{
 						PreparedStatement ps = connection.prepareStatement(
 							"update bag_mutaties_file set" +
 							" status = ?" +
 							" where id = ?"
 						);
 						ps.setInt(1,status.ordinal());
 						ps.setLong(2,id);
 						return ps;
 					}
 				}
 			);
 		}
 		catch (DataAccessException e)
 		{
 			throw new DAOException(e);
 		}
 	}
 	
 	@Override
 	public List<BAGMutatie> getNextBAGMutaties()
 	{
 		try
 		{
 			List<BAGMutatie> result = jdbcTemplate.query(
 					"select id, tijdstip_verwerking, volgnr_verwerking, object_type, mutatie_product" +
 					" from bag_mutatie" +
 					" where tijdstip_verwerking = (select min(tijdstip_verwerking) from bag_mutatie)" +
 					" order by volgnr_verwerking asc",
 				new RowMapper<BAGMutatie>()
 				{
 					@Override
 					public BAGMutatie mapRow(ResultSet rs, int row) throws SQLException
 					{
 						try
 						{
 							BAGMutatie result = new BAGMutatie();
 							result.setId(rs.getLong("id"));
 							result.setTijdstipVerwerking(rs.getTimestamp("tijdstip_verwerking"));
 							result.setVolgnrVerwerking(rs.getInt("volgnr_verwerking"));
 							result.setObjectType(BAGObjectType.values()[rs.getInt("object_type")]);
 							result.setMutatieProduct(XMLMessageBuilder.getInstance(MutatieProduct.class).handle(rs.getString("mutatie_product"),MutatieProduct.class));
 							return result;
 						}
 						catch (JAXBException e)
 						{
 							throw new DAOException(e);
 						}
 					}
 				}
 			);
 			return result;
 		}
 		catch (DataAccessException e)
 		{
 			throw new DAOException(e);
 		}
 	}
 	
 	@Override
 	public void insert(final BAGMutatie mutatie) throws DAOException
 	{
 		try
 		{
 			jdbcTemplate.update(
 				new PreparedStatementCreator()
 				{
 					@Override
 					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException
 					{
 						try
 						{
 							PreparedStatement ps = connection.prepareStatement(
 								"insert into bag_mutatie (" +
 									"id," +
 									"tijdstip_verwerking," +
 									"volgnr_verwerking," +
 									"object_type," +
 									"mutatie_product" +
 								") values ((select nvl(max(id),0) + 1 from bag_mutatie),?,?,?,?)"
 							);
 							ps.setTimestamp(1,new Timestamp(mutatie.getTijdstipVerwerking().getTime()));
 							ps.setLong(2,mutatie.getVolgnrVerwerking());
 							ps.setInt(3,mutatie.getObjectType().ordinal());
 							ps.setString(4,XMLMessageBuilder.getInstance(MutatieProduct.class).handle(new JAXBElement<MutatieProduct>(new QName("http://www.kadaster.nl/schemas/bag-verstrekkingen/extract-producten-lvc/v20090901","Mutatie-product"),MutatieProduct.class,mutatie.getMutatieProduct())));
 							return ps;
 						}
 						catch (JAXBException e)
 						{
 							throw new DAOException(e);
 						}
 					}
 				}
 			);
 		}
 		catch (DataAccessException e)
 		{
 			throw new DAOException(e);
 		}
 	}
 
 	@Override
 	public void delete(final BAGMutatie mutatie)
 	{
 		try
 		{
 			jdbcTemplate.update(
 				new PreparedStatementCreator()
 				{
 					@Override
 					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException
 					{
 						PreparedStatement ps = connection.prepareStatement(
 							"delete from bag_mutatie" +
 							" where id = ?"
 						);
 						ps.setLong(1,mutatie.getId());
 						return ps;
 					}
 				}
 			);
 		}
 		catch (DataAccessException e)
 		{
 			throw new DAOException(e);
 		}
 	}
 
 }
