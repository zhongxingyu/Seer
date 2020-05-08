 package com.schedushare.core.timeblocks.service.impl;
 
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.inject.Inject;
 
 import org.jooq.InsertValuesStep;
 import org.jooq.Record;
 import org.jooq.Result;
 
 import com.schedushare.common.domain.dto.TimeBlockEntity;
 import com.schedushare.common.domain.dto.TimeBlocksEntity;
 import com.schedushare.common.domain.exception.SchedushareException;
 import com.schedushare.common.domain.exception.SchedushareExceptionFactory;
 import com.schedushare.core.database.SchedushareFactory;
 import com.schedushare.core.database.Tables;
 import com.schedushare.core.database.enums.TimeblockDay;
 import com.schedushare.core.database.tables.records.TimeblockRecord;
 import com.schedushare.core.schedule.service.ScheduleService;
 import com.schedushare.core.timeblocks.service.TimeBlocksService;
 
 /**
  * Implements {@link TimeBlocksService}.
  */
 public class TimeBlocksServiceImpl implements TimeBlocksService {
 
 	private SchedushareExceptionFactory schedushareExceptionFactory;
 	
 	private ScheduleService scheduleService;
 	
 	/**
 	 * Default constructor.
 	 *
 	 * @param schedushareExceptionFactory the schedushare exception factory
 	 * @param scheduleService the schedule service
 	 */
 	@Inject
 	public TimeBlocksServiceImpl(
 			final SchedushareExceptionFactory schedushareExceptionFactory,
 			final ScheduleService scheduleService) {
 		
 		this.schedushareExceptionFactory = schedushareExceptionFactory;
 		this.scheduleService = scheduleService;
 	}
 	
 	@Override
 	public TimeBlockEntity getTimeBlock(Connection connection,
 			int timeBlockId) throws SchedushareException {
 		SchedushareFactory getTimeBlockQuery = new SchedushareFactory(connection);
 		Result<Record> timeBlocksRecords = getTimeBlockQuery.select()
 				.from(Tables.TIMEBLOCK)
 				.where(Tables.TIMEBLOCK.ID.equal(timeBlockId))
 				.fetch();
 		Collection<TimeBlockEntity> timeBlockEntities = new ArrayList<TimeBlockEntity>();
 		for (Record timeBlockRecord : timeBlocksRecords) {
 			timeBlockEntities.add(new TimeBlockEntity(timeBlockRecord.getValue(Tables.TIMEBLOCK.ID),
 													  timeBlockRecord.getValue(Tables.TIMEBLOCK.START_TIME),
 													  timeBlockRecord.getValue(Tables.TIMEBLOCK.END_TIME),
 													  timeBlockRecord.getValue(Tables.TIMEBLOCK.DAY).toString(),
 													  timeBlockRecord.getValue(Tables.TIMEBLOCK.NAME),
 													  timeBlockRecord.getValue(Tables.TIMEBLOCK.TYPE),
 													  timeBlockRecord.getValue(Tables.TIMEBLOCK.LATITUDE),
 													  timeBlockRecord.getValue(Tables.TIMEBLOCK.LONGITUDE),
 													  timeBlockRecord.getValue(Tables.TIMEBLOCK.SCHEDULE_ID)));
 		}
 		if (timeBlocksRecords.isEmpty()) {
 			throw schedushareExceptionFactory.createSchedushareException("Time block does not exist.");
 		} else {
 			return timeBlockEntities.iterator().next();
 		}
 	}
 
 	@Override
 	public TimeBlocksEntity getTimeBlocksForSchedule(
 			Connection connection, int scheduleId) throws SchedushareException {
 		SchedushareFactory getTimeBlocksQuery = new SchedushareFactory(connection);
 		
 		try {
 			scheduleService.getSchedule(connection, scheduleId);
 			
 			Result<Record> timeBlocksRecords = getTimeBlocksQuery.select()
 					.from(Tables.TIMEBLOCK)
 					.where(Tables.TIMEBLOCK.SCHEDULE_ID.equal(scheduleId)).fetch();
 			Collection<TimeBlockEntity> timeBlockEntities = new ArrayList<TimeBlockEntity>();
 			for (Record timeBlockRecord : timeBlocksRecords) {
 				timeBlockEntities.add(new TimeBlockEntity(timeBlockRecord.getValue(Tables.TIMEBLOCK.ID),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.START_TIME),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.END_TIME),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.DAY).toString(),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.NAME),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.TYPE),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.LATITUDE),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.LONGITUDE),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.SCHEDULE_ID)));
 			}
 			return new TimeBlocksEntity(scheduleId, timeBlockEntities);
 		} catch (SchedushareException e) {
 			throw e;
 		} catch (Exception e) {
 			throw schedushareExceptionFactory.createSchedushareException(e.getMessage());
 		}
 	}
 
 	@Override
 	public TimeBlocksEntity createTimeBlocks(Connection connection, TimeBlocksEntity timeBlocksEntity) throws SchedushareException {
 		SchedushareFactory createTimeBlocksQuery = new SchedushareFactory(connection);
 		
 		try {
 			int scheduleId = timeBlocksEntity.getScheduleId();
 			scheduleService.getSchedule(connection, scheduleId);
 			Collection<TimeBlockEntity> timeBlocks = timeBlocksEntity.getTimeBlocks();
 			InsertValuesStep<TimeblockRecord> insertIntoTimeBlock = createTimeBlocksQuery.insertInto(Tables.TIMEBLOCK, 
 															   										 Tables.TIMEBLOCK.DAY, 
 															   										 Tables.TIMEBLOCK.END_TIME, 
 															   										 Tables.TIMEBLOCK.LATITUDE,
 															   										 Tables.TIMEBLOCK.LONGITUDE, 
 															   										 Tables.TIMEBLOCK.SCHEDULE_ID, 
 															   										 Tables.TIMEBLOCK.START_TIME,
 															   										 Tables.TIMEBLOCK.NAME,
 															   										 Tables.TIMEBLOCK.TYPE);
 			for (TimeBlockEntity timeBlock : timeBlocks) {
 						insertIntoTimeBlock.values(timeBlock.getDay(), 
 												   timeBlock.getEndTime(), 
 												   timeBlock.getLatitude(), 
 												   timeBlock.getLongitude(),
 												   scheduleId, 
 												   timeBlock.getStartTime(),
 												   timeBlock.getTimeBlockName(),
 												   timeBlock.getTimeBlockType());
 			}
 			insertIntoTimeBlock.execute();
 
 			return getTimeBlocksForSchedule(connection, scheduleId);
 		} catch (SchedushareException e) {
 			throw e;
 		} catch(Exception e) {
 			throw schedushareExceptionFactory.createSchedushareException(e.getMessage());
 		}
 	}
 
 	@Override
 	public TimeBlocksEntity updateTimeBlocks(Connection connection, TimeBlocksEntity timeBlocksEntity) throws SchedushareException {
 		SchedushareFactory updateTimeBlockQuery = new SchedushareFactory(connection);
 
 		try {
 			Collection<TimeBlockEntity> timeBlocks = timeBlocksEntity.getTimeBlocks();
 			
 			for (TimeBlockEntity timeBlock : timeBlocks) {
 				updateTimeBlockQuery.update(Tables.TIMEBLOCK)
 									.set(Tables.TIMEBLOCK.DAY, TimeblockDay.valueOf(timeBlock.getDay()))
 									.set(Tables.TIMEBLOCK.END_TIME, timeBlock.getT_endTime())
 									.set(Tables.TIMEBLOCK.LATITUDE, timeBlock.getLatitude())
 									.set(Tables.TIMEBLOCK.LONGITUDE, timeBlock.getLongitude())
 									.set(Tables.TIMEBLOCK.START_TIME, timeBlock.getT_startTime())
 									.set(Tables.TIMEBLOCK.NAME, timeBlock.getTimeBlockName())
 									.set(Tables.TIMEBLOCK.TYPE, timeBlock.getTimeBlockType())
 									.where(Tables.TIMEBLOCK.ID.equal(timeBlock.getTimeBlockId()))
 									.execute();
 			}
 		} catch (Exception e) {
 			schedushareExceptionFactory.createSchedushareException(e.getMessage());
 		}
 		return getTimeBlocksForSchedule(connection, timeBlocksEntity.getScheduleId());
 	}
 
 	@Override
 	public TimeBlocksEntity updateTimeBlocksByDay(Connection connection,
 			TimeBlocksEntity timeBlocksEntity, String day) throws SchedushareException {
 
 		SchedushareFactory updateTimeBlockQuery = new SchedushareFactory(connection);
 
 		try {
 			updateTimeBlockQuery.delete(Tables.TIMEBLOCK)
								.where(Tables.TIMEBLOCK.DAY.equal(TimeblockDay.valueOf(day))
										.and(Tables.TIMEBLOCK.SCHEDULE_ID.equal(timeBlocksEntity.getScheduleId())))
 								.execute();
 			createTimeBlocks(connection, timeBlocksEntity);
 			return getTimeBlocksForSchedule(connection, timeBlocksEntity.getScheduleId());
 		} catch (Exception e) {
 			throw schedushareExceptionFactory.createSchedushareException(e.getMessage());
 		}
 	}
 
 	@Override
 	public TimeBlockEntity deleteTimeBlock(Connection connection,
 			int timeBlockId) throws SchedushareException {
 		SchedushareFactory deleteTimeBlockQuery = new SchedushareFactory(connection);
 
 		try {	
 			TimeBlockEntity timeBlockEntity = getTimeBlock(connection, timeBlockId);
 			deleteTimeBlockQuery.delete(Tables.TIMEBLOCK)
 								.where(Tables.TIMEBLOCK.ID.equal(timeBlockId))
 								.execute();
 			return timeBlockEntity;
 		} catch (SchedushareException e) {
 			throw e;
 		} catch (Exception e) {
 			throw schedushareExceptionFactory.createSchedushareException(e.getMessage());
 		}
 	}
 
 
 }
